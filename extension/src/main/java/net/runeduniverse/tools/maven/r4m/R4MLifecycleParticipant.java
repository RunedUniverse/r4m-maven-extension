/*
 * Copyright © 2023 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.tools.maven.r4m;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.eventspy.internal.EventSpyDispatcher;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator;
import org.apache.maven.lifecycle.internal.LifecycleExecutionPlanCalculator;
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
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

import net.runeduniverse.tools.maven.r4m.api.Property;
import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.api.Settings;
import net.runeduniverse.tools.maven.r4m.eventspy.api.MavenPluginPatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.MessagePatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.PatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.PatchingEvent.Type;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskRequestCalculatorDelegate;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.scanner.api.MavenProjectScanner;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = R4MProperties.R4M_LIFECYCLE_PARTICIPANT_HINT)
public class R4MLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	public static final String ERR_FAILED_TO_LOAD_MAVEN_EXTENSION_CLASSREALM = //
			"Failed to load maven-extension ClassRealm";
	public static final String ERR_FAILED_TO_LOAD_PEM = //
			"Failed while loading pem.xml from project";

	public static final String PLEXUS_DEFAULT_MAVEN_HINT = "maven-default";

	@Requirement
	private EventSpyDispatcher dispatcher;
	@Requirement
	private Settings settings;
	@Requirement
	private ExecutionArchive archive;
	@Requirement(role = MavenProjectScanner.class)
	private List<MavenProjectScanner> mavenProjectScanner;
	@Requirement
	private PlexusContainer container;
	@Requirement
	private MavenPluginManager mavenPluginManager;
	@Requirement
	private PluginDependenciesResolver pluginDependenciesResolver;

	private final Set<Plugin> unidentifiablePlugins = new LinkedHashSet<>();

	private boolean coreExtension = false;

	/**
	 * Invoked after MavenSession instance has been created.
	 *
	 * This callback is intended to allow extensions to inject execution properties,
	 * activate profiles and perform similar tasks that affect MavenProject instance
	 * construction.
	 */
	@Override
	public void afterSessionStart(MavenSession mvnSession) throws MavenExecutionException {
		this.coreExtension = true;

		mvnSession.getSettings()
				.addPluginGroup(Runes4MavenProperties.GROUP_ID);
	}

	/**
	 * Invoked after all MavenProject instances have been created.
	 *
	 * This callback is intended to allow extensions to manipulate MavenProjects
	 * before they are sorted and actual build execution starts.
	 */
	@Override
	public void afterProjectsRead(MavenSession mvnSession) throws MavenExecutionException {
		this.mavenProjectScanner = new LinkedList<>(this.mavenProjectScanner);
		this.mavenProjectScanner.sort(null);

		compileSettings(mvnSession);

		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_PATCHING_STARTED));

		ClassRealm currentRealm = (ClassRealm) Thread.currentThread()
				.getContextClassLoader();
		ClassWorld world = currentRealm.getWorld();

		try {
			ClassRealm realm;
			if (this.coreExtension) {
				this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_ELEVATING_TO_CORE_REALM));
				realm = world.getRealm("maven.ext");
			} else {
				this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_ELEVATING_TO_BUILD_REALM));
				// we need to reinitiate the r4m-maven-extension realm because maven injects an
				// outdated version of the plexus-utils
				realm = world.getRealm("plexus.core")
						.createChildRealm("extension>net.runeduniverse.tools.maven.r4m:r4m-maven-extension");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.api");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.eventspy.api");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.model");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.parser");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.parser.trigger");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.parser.restrictions");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.writer");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.writer.trigger");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.writer.restrictions");
				realm.importFrom(currentRealm, "net.runeduniverse.lib.utils.logging.logs");
			}

			Thread.currentThread()
					.setContextClassLoader(realm);

			Collection<Plugin> extPlugins = scanCoreExtensions(world.getRealms());
			for (MavenProject mvnProject : mvnSession.getAllProjects())
				scanProject(mvnSession, extPlugins, mvnProject);

			if ("scan".equals(this.settings.getMissingBuildPluginHandler()
					.getSelected())) {
				this.dispatcher
						.onEvent(PatchingEvent.createInfoEvent(Type.INFO_SCANNING_FOR_REFERENCED_PLUGINS_STARTED));
				// collect indirectly referenced build-plugins after seeding the archive
				for (MavenProject mvnProject : mvnSession.getAllProjects())
					loadReferencedPlugins(mvnSession, mvnProject);
				this.dispatcher
						.onEvent(PatchingEvent.createInfoEvent(Type.INFO_SCANNING_FOR_REFERENCED_PLUGINS_FINISHED));
			}

			modifyLifecycleExecutionPlanCalculator();

		} catch (DuplicateRealmException | NoSuchRealmException e) {
			MavenExecutionException ex = new MavenExecutionException(ERR_FAILED_TO_LOAD_MAVEN_EXTENSION_CLASSREALM, e);
			this.dispatcher.onEvent(PatchingEvent.createErrorEvent(Type.INFO_PATCHING_ABORTED, ex));
			throw ex;
		} catch (Exception e) {
			MavenExecutionException ex = new MavenExecutionException(ERR_FAILED_TO_LOAD_PEM, e);
			this.dispatcher.onEvent(PatchingEvent.createErrorEvent(Type.INFO_PATCHING_ABORTED, ex));
			throw ex;
		} finally {
			this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_RETURNING_TO_EXTENSION_REALM));
			Thread.currentThread()
					.setContextClassLoader(currentRealm);
		}

		if (!this.unidentifiablePlugins.isEmpty())
			this.dispatcher.onEvent(MavenPluginPatchingEvent.createInfoEvent(Type.WARN_UNIDENTIFIABLE_PLUGIN_DETECTED,
					this.unidentifiablePlugins));

		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_PATCHING_FINISHED));
	}

	private void compileSettings(final MavenSession mvnSession) {
		Properties prop = defaultProperties();
		prop.putAll(mvnSession.getSystemProperties());
		prop.putAll(mvnSession.getCurrentProject()
				.getProperties());
		prop.putAll(mvnSession.getUserProperties());

		this.settings.setLifecycleTaskRequestCalculator(buildTextPropertyAddPlexusHints(prop,
				"r4m.lifecycle-task-request-calculator", LifecycleTaskRequestCalculatorDelegate.class));
		this.settings.setLifecycleTaskRequestCalculatorOnFork(buildTextPropertyAddPlexusHints(prop,
				"r4m.lifecycle-task-request-calculator-on-fork", LifecycleTaskRequestCalculatorDelegate.class));
		this.settings.setMissingBuildPluginHandler(
				buildTextProperty(prop, "r4m.missing-build-plugin-handler", "skip", "warn", "scan", "download"));
		this.settings.setActiveProfilesInheritance(
				buildTextProperty(prop, "r4m.active-profiles-inheritance", "upstream", "top-level", "false"));
		this.settings.setPatchMojoOnFork(buildBooleanProperty(prop, "r4m.patch-mojo-on-fork"));
		this.settings.setGeneratePluginExecutions(buildBooleanProperty(prop, "r4m.generate-plugin-executions"));
		this.settings.setGeneratePluginExecutionsOnFork(
				buildBooleanProperty(prop, "r4m.generate-plugin-executions-on-fork"));

		this.settings.selectDefaults();
	}

	private Properties defaultProperties() {
		Properties properties = new Properties();

		properties.setProperty("r4m.lifecycle-task-request-calculator.default", "declared");
		properties.setProperty("r4m.lifecycle-task-request-calculator-on-fork.default", "sequential");
		properties.setProperty("r4m.missing-build-plugin-handler.default", "warn");
		properties.setProperty("r4m.active-profiles-inheritance.default", "upstream");
		properties.setProperty("r4m.patch-mojo-on-fork.default", "true");
		properties.setProperty("r4m.generate-plugin-executions.default", "true");
		properties.setProperty("r4m.generate-plugin-executions-on-fork.default", "true");

		return properties;
	}

	private Property<Boolean> buildBooleanProperty(final Properties properties, final String key) {
		AbstractProperty<Boolean> property = new AbstractProperty<>(key);
		String defaultValue = properties.getProperty(key + ".default");
		String selectedValue = properties.getProperty(key);
		property.setDefault(defaultValue == null ? null : "true".equals(defaultValue));
		property.setSelected(selectedValue == null ? null : "true".equals(selectedValue));
		property.add(true, false);
		return property;
	}

	private Property<String> buildTextProperty(final Properties properties, final String key, final String... options) {
		AbstractProperty<String> property = new AbstractProperty<>(key);
		property.setDefault(properties.getProperty(key + ".default"));
		property.setSelected(properties.getProperty(key));
		property.add(options);
		return property;
	}

	private Property<String> buildTextPropertyAddPlexusHints(final Properties properties, final String key,
			final Class<?> plexusRole) {
		AbstractProperty<String> property = new AbstractProperty<>(key);
		property.setDefault(properties.getProperty(key + ".default"));
		property.setSelected(properties.getProperty(key));
		try {
			property.addAll(this.container.lookupMap(plexusRole)
					.keySet());
		} catch (ComponentLookupException e) {
		}
		return property;
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

		for (MavenProjectScanner scanner : this.mavenProjectScanner)
			scanner.scan(mvnSession, extPlugins, this.unidentifiablePlugins, mvnProject, projectSlice);
	}

	private void loadReferencedPlugins(final MavenSession mvnSession, final MavenProject mvnProject) {
		Map<String, CharSequence> eventData = new LinkedHashMap<>();
		eventData.put("maven-project", mvnProject.getGroupId() + ':' + mvnProject.getArtifactId());

		this.dispatcher.onEvent(MessagePatchingEvent
				.createInfoEvent(Type.INFO_SCANNING_FOR_REFERENCED_PLUGINS_BY_PROJECT_STARTED, eventData)
				.readonly());

		PluginContainer plugins = mvnProject.getBuild();
		List<Artifact> knownArtifacts = new LinkedList<>();
		List<Plugin> knownPlugins = new LinkedList<>(plugins.getPlugins());
		List<Plugin> remainingPlugins = plugins.getPlugins();
		Map<String, Plugin> managedPlugins = mvnProject.getBuild()
				.getPluginManagement()
				.getPluginsAsMap();
		int discoveredPluginAmount = 0;
		while (!remainingPlugins.isEmpty()) {
			List<Plugin> cache = new LinkedList<>();
			for (Plugin plugin : remainingPlugins)
				cache.addAll(discoverReferencedPlugins(mvnSession.getRepositorySession(), mvnProject, knownArtifacts,
						knownPlugins, managedPlugins, plugin));
			remainingPlugins = cache;
			discoveredPluginAmount = discoveredPluginAmount + cache.size();
			mvnProject.getBuild()
					.getPlugins()
					.addAll(cache);
		}

		eventData.put("amount", "" + discoveredPluginAmount);
		this.dispatcher.onEvent(MessagePatchingEvent
				.createInfoEvent(Type.INFO_SCANNING_FOR_REFERENCED_PLUGINS_BY_PROJECT_FINISHED, eventData));
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

	protected void modifyLifecycleExecutionPlanCalculator() {
		String defaultExecPlanCalcName = DefaultLifecycleExecutionPlanCalculator.class.getCanonicalName();
		DefaultLifecycleExecutionPlanCalculator defaultExecPlanCalc = null;

		Map<String, CharSequence> eventData = new LinkedHashMap<>();
		eventData.put("component", defaultExecPlanCalcName);
		eventData.put("role", LifecycleExecutionPlanCalculator.class.getCanonicalName());

		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_LIFECYCLE_EXEC_PLAN_CALC_STARTED));
		try {
			for (LifecycleExecutionPlanCalculator item : this.container
					.lookupList(LifecycleExecutionPlanCalculator.class))
				if (item instanceof DefaultLifecycleExecutionPlanCalculator) {
					defaultExecPlanCalc = (DefaultLifecycleExecutionPlanCalculator) item;
					break;
				}
		} catch (ComponentLookupException e) {
			this.dispatcher.onEvent(MessagePatchingEvent
					.createInfoEvent(Type.WARN_LIFECYCLE_EXEC_PLAN_CALC_FAILED_TO_LOCATE_PLEXUS_COMPONENT, eventData)
					.readonly());
		}
		if (defaultExecPlanCalc != null)
			try {
				this.container.release(defaultExecPlanCalc);
				this.container.addComponent(defaultExecPlanCalc, DefaultLifecycleExecutionPlanCalculator.class,
						PLEXUS_DEFAULT_MAVEN_HINT);
				eventData.put("role", defaultExecPlanCalcName);
				eventData.put("hint", PLEXUS_DEFAULT_MAVEN_HINT);
				this.dispatcher.onEvent(MessagePatchingEvent
						.createInfoEvent(Type.DEBUG_LIFECYCLE_EXEC_PLAN_CALC_UPDATING_PLEXUS_COMPONENT_DESCRIPTOR,
								eventData)
						.readonly());
			} catch (ComponentLifecycleException e) {
				this.dispatcher.onEvent(MessagePatchingEvent
						.createInfoEvent(Type.WARN_LIFECYCLE_EXEC_PLAN_CALC_FAILED_TO_RELEASE_PLEXUS_COMPONENT,
								eventData)
						.readonly());
			}
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_LIFECYCLE_EXEC_PLAN_CALC_FINISHED));
	}

	private static Collection<Plugin> scanCoreExtensions(final Collection<ClassRealm> realms) {
		Collection<Plugin> extPlugins = new LinkedHashSet<>();
		for (ClassRealm realm : realms) {
			Plugin plugin = fromExtRealm(realm);
			if (plugin == null)
				continue;
			extPlugins.add(plugin);
		}
		return Collections.unmodifiableCollection(extPlugins);
	}

	private static Plugin fromExtRealm(ClassRealm realm) {
		String id = realm.getId();
		if (!id.startsWith("coreExtension>"))
			return null;
		Plugin plugin = new Plugin();
		plugin.setExtensions(true);
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
