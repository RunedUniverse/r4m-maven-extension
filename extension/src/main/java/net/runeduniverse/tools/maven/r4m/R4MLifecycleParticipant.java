/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
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
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

import net.runeduniverse.lib.utils.maven.ext.MvnCorePatcher;
import net.runeduniverse.lib.utils.maven.ext.api.ExtensionIndex;
import net.runeduniverse.lib.utils.maven.ext.data.api.Extension;
import net.runeduniverse.lib.utils.maven.ext.data.api.ExtensionData;
import net.runeduniverse.lib.utils.maven.ext.data.api.PluginData;
import net.runeduniverse.lib.utils.maven.ext.eventspy.api.EventSpyDispatcherProxy;
import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.api.Settings;
import net.runeduniverse.tools.maven.r4m.api.Settings.LoadState;
import net.runeduniverse.tools.maven.r4m.eventspy.api.ExtensionPatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.MavenPluginPatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.MessagePatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.PatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.PatchingEvent.Type;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.scanner.api.MavenProjectScanner;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = R4MProperties.R4M_LIFECYCLE_PARTICIPANT_HINT)
public class R4MLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	public static final String ERR_FAILED_TO_LOOKUP_EVENT_SPY_DISPATCHER = //
			"Failed to lookup EventSpyDispatcher from Maven-Core";
	public static final String ERR_FAILED_TO_LOAD_MODELS = //
			"Failed while loading pem.xml & grm.xml from project";
	public static final String ERR_FAILED_TO_RESOLVE_PLUGIN_ARTIFACT = //
			"Failed to resolve plugin-artifact";
	public static final String ERR_FAILED_TO_RESOLVE_PLUGIN = //
			"Failed to resolve plugin";

	public static final String PLEXUS_DEFAULT_MAVEN_HINT = "maven-default";

	@Requirement
	private Logger log;
	@Requirement
	private PlexusContainer container;
	@Requirement
	private EventSpyDispatcherProxy dispatcher;
	@Requirement
	private Settings settings;
	@Requirement
	private SettingsFactory settingsFactory;
	@Requirement
	private ExtensionIndex extIndex;
	@Requirement
	private ExecutionArchive pemArchive;
	@Requirement
	private GoalRequirementArchive grmArchive;
	@Requirement(role = MavenProjectScanner.class)
	private List<MavenProjectScanner> mavenProjectScanner;
	@Requirement
	private PluginDependenciesResolver pluginDependenciesResolver;
	@Requirement
	protected MavenPluginManager mavenPluginManager;

	// private final Set<Extension> extensions = new LinkedHashSet<>();
	private final Map<MavenProject, Set<Plugin>> invalidPlugins = new ConcurrentHashMap<>();

	private MvnCorePatcher mvnCorePatcher = null;

	private MvnCorePatcher getMvnCorePatcher() {
		if (this.mvnCorePatcher != null)
			return this.mvnCorePatcher;
		return this.mvnCorePatcher = new MvnCorePatcher(this.extIndex);
	}

	/**
	 * Invoked after MavenSession instance has been created.
	 *
	 * This callback is intended to allow extensions to inject execution properties,
	 * activate profiles and perform similar tasks that affect MavenProject instance
	 * construction.
	 */
	@Override
	public void afterSessionStart(final MavenSession mvnSession) throws MavenExecutionException {
		final ClassRealm currentRealm = (ClassRealm) Thread.currentThread()
				.getContextClassLoader();
		try {
			this.dispatcher.locateDispatcherProxy(currentRealm);
		} catch (ComponentLookupException e) {
			this.log.fatalError(ERR_FAILED_TO_LOOKUP_EVENT_SPY_DISPATCHER, e);
		}

		getMvnCorePatcher().flagAsCoreExtension();

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
	public void afterProjectsRead(final MavenSession mvnSession) throws MavenExecutionException {
		this.mavenProjectScanner = new LinkedList<>(this.mavenProjectScanner);
		this.mavenProjectScanner.sort(null);

		this.settingsFactory.setup(mvnSession);

		setupMvnCorePatcher(mvnSession);

		getMvnCorePatcher().patchMaven(mvnSession, this::patchMaven);
	}

	private void setupMvnCorePatcher(final MavenSession mvnSession) {
		final MvnCorePatcher patcher = getMvnCorePatcher();

		patcher.withExtensionRealmFactory(this::createExtensionRealm);
		patcher.withBuildExtensionSupport(this.dispatcher.getFeatureState());
		// register event hooks
		patcher.onInfo_PatchingStarted(this::_patchEventInfo_PatchingStarted);
		patcher.onInfo_SwitchRealmToPlexus(this::_patchEventInfo_SwitchRealmToPlexus);
		patcher.onInfo_SwitchRealmToMavenExt(this::_patchEventInfo_SwitchRealmToMavenExt);
		patcher.onInfo_SwitchRealmToBuildExt(this::_patchEventInfo_SwitchRealmToBuildExt);
		patcher.onInfo_InvalidBuildExtension(() -> {
			mvnSession.getSettings()
					.addPluginGroup(Runes4MavenProperties.GROUP_ID);
			R4MLifecycleParticipant.this._patchEventInfo_InvalidBuildExt();
		});
		patcher.onError_PatchingAborted(this::_patchEventError_PatchingAborted);
		patcher.onInfo_ResetRealm(this::_patchEventInfo_ResetRealm);
		patcher.onInfo_ExtensionsDetected(this::_patchEventInfo_ExtensionsDetected);
		patcher.onInfo_InvalidPluginsDetected(this::_patchEventInfo_InvalidPluginsDetected);
		patcher.onInfo_PatchingFinished(this::_patchEventInfo_PatchingFinished);
	}

	private ClassRealm createExtensionRealm(final ClassRealm plexusCore, final ClassRealm currentRealm)
			throws DuplicateRealmException {
		// we need to reinitiate the r4m-maven-extension realm because maven injects an
		// outdated version of the plexus-utils
		final ClassRealm realm = plexusCore
				.createChildRealm("extension>net.runeduniverse.tools.maven.r4m:r4m-maven-extension");
		realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m");
		realm.importFrom(currentRealm, "net.runeduniverse.lib.utils");
		return realm;
	}

	private boolean patchMaven(final MavenSession mvnSession, final boolean isCoreExt,
			final Map<MavenProject, Set<Extension>> extensions, final Map<MavenProject, Set<Plugin>> extPlugins)
			throws MavenExecutionException {

		try {
			for (MavenProject mvnProject : mvnSession.getAllProjects())
				scanProject(mvnSession, extPlugins.getOrDefault(mvnProject, Collections.emptySet()), mvnProject);
		} catch (Exception e) {
			throw new MavenExecutionException(ERR_FAILED_TO_LOAD_MODELS, e);
		}

		if ("scan".equals(this.settings.getMissingBuildPluginHandler()
				.getSelected())) {
			this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_SCANNING_FOR_REFERENCED_PLUGINS_STARTED));
			// collect indirectly referenced build-plugins after seeding the archive
			for (MavenProject mvnProject : mvnSession.getAllProjects())
				loadReferencedPlugins(mvnSession, mvnProject);
			this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_SCANNING_FOR_REFERENCED_PLUGINS_FINISHED));
		}

		modifyLifecycleExecutionPlanCalculator();
		return true;
	}

	private void scanProject(final MavenSession mvnSession, final Collection<Plugin> extPlugins,
			final MavenProject mvnProject) throws Exception {
		this.pemArchive.createSector(mvnProject);
		this.grmArchive.createSector(mvnProject);

		final Set<Plugin> invalidPlugins = this.invalidPlugins.computeIfAbsent(mvnProject,
				k -> ConcurrentHashMap.newKeySet());

		mvnProject.getBuild()
				.getPlugins()
				.addAll(extPlugins);

		for (MavenProjectScanner scanner : this.mavenProjectScanner)
			scanner.scan(mvnSession, extPlugins, invalidPlugins, mvnProject);
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
		final List<Plugin> referencedPlugins = new LinkedList<>();
		List<Artifact> artifacts = null;
		try {
			artifacts = resolvePluginArtifacts(parentPlugin, mvnProject.getRemotePluginRepositories(), repoSession);
		} catch (PluginResolutionException ignored) {
			if (this.log.isDebugEnabled())
				this.log.debug(ERR_FAILED_TO_RESOLVE_PLUGIN_ARTIFACT, ignored);
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
					| PluginResolutionException ignored) {
				// probably not a plugin ...
				if (this.log.isDebugEnabled())
					this.log.debug(ERR_FAILED_TO_RESOLVE_PLUGIN, ignored);
				continue;
			}

			knownPlugins.add(plugin);
			referencedPlugins.add(plugin);
		}
		return referencedPlugins;
	}

	private List<Artifact> resolvePluginArtifacts(Plugin extensionPlugin, List<RemoteRepository> repositories,
			RepositorySystemSession session) throws PluginResolutionException {
		final DependencyNode root = pluginDependenciesResolver.resolve(extensionPlugin, null, null, repositories,
				session);
		final PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
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

	private void modifyLifecycleExecutionPlanCalculator() {
		final String defaultExecPlanCalcName = DefaultLifecycleExecutionPlanCalculator.class.getCanonicalName();
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

	private void _patchEventInfo_PatchingStarted() {
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_PATCHING_STARTED));
	}

	private void _patchEventInfo_SwitchRealmToPlexus() {
		this.settings.setLoadState(LoadState.SYSTEM_EXTENSION);
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_ELEVATING_TO_PLEXUS_REALM));
	}

	private void _patchEventInfo_SwitchRealmToMavenExt() {
		this.settings.setLoadState(LoadState.CORE_EXTENSION);
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_ELEVATING_TO_CORE_REALM));
	}

	private void _patchEventInfo_SwitchRealmToBuildExt() {
		this.settings.setLoadState(LoadState.BUILD_EXTENSION);
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_ELEVATING_TO_BUILD_REALM));
	}

	private void _patchEventInfo_InvalidBuildExt() {
		this.settings.setLoadState(LoadState.BUILD_EXTENSION);
		this.log.fatalError("╔══════════════════════════════════════════════════════════════════════╗");
		this.log.fatalError("║                            \033[1m! ATTENTION !\033[m                             ║");
		this.log.fatalError("║                                                                      ║");
		this.log.fatalError("║ If you are seeing this message, loading R4M as a build-extension     ║");
		this.log.fatalError("║    is not supported on your version of Maven!                        ║");
		this.log.fatalError("╟──────────────────────────────────────────────────────────────────────╢");
		this.log.fatalError("║ To resolve this R4M has to be configured as a core-extension!        ║");
		this.log.fatalError("║   Please check your configuration!                                   ║");
		this.log.fatalError("║                                                                      ║");
		this.log.fatalError("║   Quick Fix: 'mvn r4m:prj-setup'                                     ║");
		this.log.fatalError("║                                                                      ║");
		this.log.fatalError("╟──────────────────────────────────────────────────────────────────────╢");
		this.log.fatalError("║  Maven Patching was be skipped - most functionality is unavailable!  ║");
		this.log.fatalError("╚══════════════════════════════════════════════════════════════════════╝");
	}

	private void _patchEventError_PatchingAborted(final MavenExecutionException ex) {
		this.dispatcher.onEvent(PatchingEvent.createErrorEvent(Type.INFO_PATCHING_ABORTED, ex));
	}

	private void _patchEventInfo_ResetRealm() {
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_RETURNING_TO_EXTENSION_REALM));
	}

	private void _patchEventInfo_ExtensionsDetected(final Collection<MavenProject> allProjects,
			final ExtensionData data) {
		this.dispatcher.onEvent(
				ExtensionPatchingEvent.createInfoEvent(Type.INFO_EXTENSIONS_DETECTED, allProjects, data.getExtensions())
						.readonly());
	}

	private void _patchEventInfo_InvalidPluginsDetected(final Collection<MavenProject> projects,
			final PluginData data) {
		// add invalid extension plugins
		for (Entry<MavenProject, Set<Plugin>> entry : data.getPluginMap()
				.entrySet()) {
			final Set<Plugin> set = this.invalidPlugins.computeIfAbsent(entry.getKey(),
					k -> ConcurrentHashMap.newKeySet());
			set.addAll(entry.getValue());
		}
		// exec event with all known invalid plugins
		this.dispatcher.onEvent(
				MavenPluginPatchingEvent.createInfoEvent(Type.WARN_INVALID_PLUGIN_DETECTED, this.invalidPlugins));
	}

	private void _patchEventInfo_PatchingFinished() {
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_PATCHING_FINISHED));
	}
}
