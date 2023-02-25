package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelConfigParser;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelPackagingParser;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelPluginParser;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = Properties.EXECUTIONS_PARSER_LIFECYCLE_PARTICIPANT_HINT)
public class ProjectExecutionModelLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	public static final String ERR_FAILED_LOADING_MAVEN_EXTENSION_CLASSREALM = "Failed loading maven-extension ClassRealm";

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
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.api.pem");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.api.pem.model");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.parser");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.parser.trigger");
				realm.importFrom(currentRealm, "net.runeduniverse.lib.utils.logging.logs");
			}

			Collection<Plugin> extPlugins = scanCoreExtensions(world.getRealms());

			Thread.currentThread()
					.setContextClassLoader(realm);

			for (MavenProject mvnProject : mvnSession.getAllProjects())
				scanProject(mvnSession, extPlugins, mvnProject);

		} catch (Exception e) {
			throw new MavenExecutionException(ERR_FAILED_LOADING_MAVEN_EXTENSION_CLASSREALM, e);
		} finally {
			Thread.currentThread()
					.setContextClassLoader(currentRealm);
		}
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
				projectSlice.register(parser.parse(mvnProject.getRemotePluginRepositories(),
						mvnSession.getRepositorySession(), mvnPlugin));

		for (ProjectExecutionModelPackagingParser parser : this.pemPackagingParser.values())
			projectSlice.register(parser.parse());
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
