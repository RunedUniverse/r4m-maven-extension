package net.runeduniverse.tools.maven.r4m.pem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.internal.PluginDependenciesResolver;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelConfigParser;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelPackagingParser;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelPluginParser;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = Properties.EXECUTIONS_PARSER_LIFECYCLE_PARTICIPANT_HINT)
public class ProjectExecutionModelLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	public static final String ERR_FAILED_LOADING_MAVEN_EXTENSION_CLASSREALM = "Failed loading maven-extension ClassRealm";
	public static final String ERR_UNIDENTIFIABLE_PLUGIN_DETECTED_HEAD = "\033[1;31mFollowing Plugins or one of their dependencies could not be resolved: "
			+ "They were not found in https://repo.maven.apache.org/maven2 during a previous attempt. "
			+ "This failure was cached in the local repository and resolution is not reattempted until the update interval "
			+ "of central has elapsed or updates are forced\u001B[0m";
	public static final String ERR_UNIDENTIFIABLE_PLUGIN_DETECTED = "Unidentifiable plugin detected » %s:%s:%s";

	@Requirement
	private Logger log;
	@Requirement
	private ExecutionArchive archive;
	@Requirement(role = ProjectExecutionModelConfigParser.class)
	private Map<String, ProjectExecutionModelConfigParser> pemConfigParser;
	@Requirement(role = ProjectExecutionModelPluginParser.class)
	private Map<String, ProjectExecutionModelPluginParser> pemPluginParser;
	@Requirement(role = ProjectExecutionModelPackagingParser.class)
	private Map<String, ProjectExecutionModelPackagingParser> pemPackagingParser;
	@Requirement
	private PlexusContainer container;
	@Requirement
	private MavenPluginManager mavenPluginManager;
	@Requirement
	private PluginDependenciesResolver pluginDependenciesResolver;

	private final Set<Plugin> unidentifiablePlugins = new LinkedHashSet<>();

	private boolean coreExtension = false;

	public void afterSessionStart(MavenSession mvnSession) throws MavenExecutionException {
		this.coreExtension = true;

		mvnSession.getSettings()
				.addPluginGroup(Properties.GROUP_ID);
	}

	/**
	 * Invoked after all MavenProject instances have been created.
	 *
	 * This callback is intended to allow extensions to manipulate MavenProjects
	 * before they are sorted and actual build execution starts.
	 */
	public void afterProjectsRead(MavenSession mvnSession) throws MavenExecutionException {
		ClassRealm currentRealm = (ClassRealm) Thread.currentThread()
				.getContextClassLoader();
		ClassWorld world = currentRealm.getWorld();

		try {
			ClassRealm realm;
			if (this.coreExtension)
				realm = world.getRealm("maven.ext");
			else {
				// we need to reinitiate the r4m-maven-extension realm because maven injects an
				// outdated version of the plexus-utils
				realm = world.getRealm("plexus.core")
						.createChildRealm("extension>net.runeduniverse.tools.maven.r4m:r4m-maven-extension");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.api");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.model");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.parser");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.parser.trigger");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.parser.restrictions");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.writer");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.writer.trigger");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.writer.restrictions");
				realm.importFrom(currentRealm, "net.runeduniverse.lib.utils.logging.logs");
			}

			Collection<Plugin> extPlugins = scanCoreExtensions(world.getRealms());

			Thread.currentThread()
					.setContextClassLoader(realm);

			for (MavenProject mvnProject : mvnSession.getAllProjects())
				scanProject(mvnSession, extPlugins, mvnProject);

			// collect indirectly referenced build-plugins after seeding the archive
			for (MavenProject mvnProject : mvnSession.getAllProjects())
				// enable dependent on property?
				loadReferencedPlugins(mvnSession, mvnProject);

		} catch (Exception e) {
			throw new MavenExecutionException(ERR_FAILED_LOADING_MAVEN_EXTENSION_CLASSREALM, e);
		} finally {
			Thread.currentThread()
					.setContextClassLoader(currentRealm);
		}

		logUnidentifiablePlugins();
	}

	private void scanProject(final MavenSession mvnSession, final Collection<Plugin> extPlugins,
			final MavenProject mvnProject) throws Exception {

		ExecutionArchiveSlice projectSlice = this.archive.getSlice(mvnProject);
		if (projectSlice == null)
			projectSlice = this.archive.createSlice(mvnProject);
		else
			return;

		mvnProject.getBuild()
				.getPlugins()
				.addAll(extPlugins);

		for (ProjectExecutionModelConfigParser parser : this.pemConfigParser.values())
			projectSlice.register(parser.parse(mvnProject));

		for (ProjectExecutionModelPluginParser parser : this.pemPluginParser.values())
			for (Plugin mvnPlugin : mvnProject.getBuildPlugins())
				if (isIdentifiable(mvnPlugin))
					try {
						projectSlice.register(parser.parse(mvnProject.getRemotePluginRepositories(),
								mvnSession.getRepositorySession(), mvnPlugin));
					} catch (PluginResolutionException e) {
						this.unidentifiablePlugins.add(mvnPlugin);
					}

		for (ProjectExecutionModelPackagingParser parser : this.pemPackagingParser.values())
			projectSlice.register(parser.parse());
	}

	private boolean isIdentifiable(Plugin mvnPlugin) {
		if (this.unidentifiablePlugins.contains(mvnPlugin))
			return false;

		if (mvnPlugin.getVersion() == null) {
			this.unidentifiablePlugins.add(mvnPlugin);
			return false;
		}

		return true;
	}

	private void logUnidentifiablePlugins() {
		if (this.unidentifiablePlugins.isEmpty())
			return;

		this.log.error(ERR_UNIDENTIFIABLE_PLUGIN_DETECTED_HEAD);
		for (Plugin mvnPlugin : this.unidentifiablePlugins)
			this.log.error(String.format(ERR_UNIDENTIFIABLE_PLUGIN_DETECTED, mvnPlugin.getGroupId(),
					mvnPlugin.getArtifactId(), mvnPlugin.getVersion()));
	}

	private void loadReferencedPlugins(final MavenSession mvnSession, final MavenProject mvnProject) {
		PluginContainer plugins = mvnProject.getBuild();
		List<Artifact> knownArtifacts = new LinkedList<>();
		List<Plugin> knownPlugins = new LinkedList<>(plugins.getPlugins());
		List<Plugin> remainingPlugins = plugins.getPlugins();
		Map<String, Plugin> managedPlugins = mvnProject.getBuild()
				.getPluginManagement()
				.getPluginsAsMap();
		while (!remainingPlugins.isEmpty()) {
			List<Plugin> cache = new LinkedList<>();
			for (Plugin plugin : remainingPlugins)
				cache.addAll(discoverReferencedPlugins(mvnSession.getRepositorySession(), mvnProject, knownArtifacts,
						knownPlugins, managedPlugins, plugin));
			remainingPlugins = cache;
			mvnProject.getBuild()
					.getPlugins()
					.addAll(cache);
		}
	}

	private List<Plugin> discoverReferencedPlugins(final RepositorySystemSession repoSession,
			final MavenProject mvnProject, final List<Artifact> knownArtifacts, final List<Plugin> knownPlugins,
			final Map<String, Plugin> managedPlugins, final Plugin parentPlugin) {
		List<Plugin> referencedPlugins = new LinkedList<>();
		List<Artifact> artifacts = null;
		try {
			artifacts = resolvePluginArtifacts(parentPlugin, mvnProject.getRemotePluginRepositories(), repoSession);
		} catch (PluginResolutionException e) {
		}
		if (artifacts == null)
			return referencedPlugins;

		for (Artifact artifact : artifacts) {
			if (knownArtifacts.contains(artifact))
				continue;
			knownArtifacts.add(artifact);

			Plugin plugin = new Plugin();
			plugin.setGroupId(artifact.getGroupId());
			plugin.setArtifactId(artifact.getArtifactId());
			plugin.setVersion(artifact.getVersion());

			Plugin pluginInPom = managedPlugins.get(plugin.getKey());
			if (pluginInPom != null) {
				if (plugin.getVersion() == null)
					plugin.setVersion(pluginInPom.getVersion());
				plugin.setDependencies(new ArrayList<>(pluginInPom.getDependencies()));
			}

			if (knownPlugins.contains(plugin))
				continue;

			try {
				this.mavenPluginManager.getPluginDescriptor(plugin, mvnProject.getRemotePluginRepositories(),
						repoSession);
			} catch (PluginDescriptorParsingException | InvalidPluginDescriptorException
					| PluginResolutionException e) {
				// probably not a plugin ...
				continue;
			}

			knownPlugins.add(plugin);
			referencedPlugins.add(plugin);
		}
		return referencedPlugins;
	}

	private List<Artifact> resolvePluginArtifacts(Plugin extensionPlugin, List<RemoteRepository> repositories,
			RepositorySystemSession session) throws PluginResolutionException {
		DependencyNode root = pluginDependenciesResolver.resolve(extensionPlugin, null, null, repositories, session);
		PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
		root.accept(nlg);

		List<Artifact> artifacts = new ArrayList<>(nlg.getNodes()
				.size());
		RepositoryUtils.toArtifacts(artifacts, Collections.singleton(root), Collections.<String>emptyList(), null);
		for (Iterator<Artifact> it = artifacts.iterator(); it.hasNext();) {
			Artifact artifact = it.next();
			if (artifact.getFile() == null) {
				it.remove();
			}
		}
		return Collections.unmodifiableList(artifacts);
	}

	private static Collection<Plugin> scanCoreExtensions(final Collection<ClassRealm> realms) {
		Collection<Plugin> extPlugins = new LinkedHashSet<Plugin>();
		for (ClassRealm realm : realms) {
			Plugin plugin = fromExtRealm(realm);
			if (plugin == null)
				continue;
			extPlugins.add(plugin);
		}
		return extPlugins;
	}

	private static Plugin fromExtRealm(ClassRealm realm) {
		String id = realm.getId();
		Plugin plugin = new Plugin();
		plugin.setExtensions(true);
		if (!id.startsWith("coreExtension>"))
			return null;
		id = id.substring(14);
		int idx = id.indexOf(':');
		plugin.setGroupId(id.substring(0, idx));
		id = id.substring(idx + 1);
		idx = id.indexOf(':');
		plugin.setArtifactId(id.substring(0, idx));
		plugin.setVersion(id.substring(idx + 1));
		return plugin;
	}

}
