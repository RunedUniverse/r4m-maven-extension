/*
 * Copyright © 2024 VenaNocta (venanocta@gmail.com)
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
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.internal.PluginDependenciesResolver;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

import net.runeduniverse.lib.utils.maven.ext.BootstrapMavenLifecycleParticipant;
import net.runeduniverse.lib.utils.maven.ext.api.Extension;
import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.api.Settings;
import net.runeduniverse.tools.maven.r4m.eventspy.api.MavenPluginPatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.MessagePatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.PatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.PatchingEvent.Type;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.scanner.api.MavenProjectScanner;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = R4MProperties.R4M_LIFECYCLE_PARTICIPANT_HINT)
public class R4MLifecycleParticipant extends BootstrapMavenLifecycleParticipant {

	public static final String ERR_FAILED_TO_LOAD_MODELS = //
			"Failed while loading pem.xml & grm.xml from project";

	public static final String PLEXUS_DEFAULT_MAVEN_HINT = "maven-default";

	@Requirement
	private EventSpyDispatcher dispatcher;
	@Requirement
	private Settings settings;
	@Requirement
	private SettingsFactory settingsFactory;
	@Requirement
	private ExecutionArchive pemArchive;
	@Requirement
	private GoalRequirementArchive grmArchive;
	@Requirement(role = MavenProjectScanner.class)
	private List<MavenProjectScanner> mavenProjectScanner;
	@Requirement
	private PluginDependenciesResolver pluginDependenciesResolver;

	private final Set<Extension> extensions = new LinkedHashSet<>();

	/**
	 * Invoked after MavenSession instance has been created.
	 *
	 * This callback is intended to allow extensions to inject execution properties,
	 * activate profiles and perform similar tasks that affect MavenProject instance
	 * construction.
	 */
	@Override
	public void afterSessionStart(MavenSession mvnSession) throws MavenExecutionException {
		super.afterSessionStart(mvnSession);

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

		this.settingsFactory.setup(mvnSession);

		patchMaven(mvnSession);
	}

	@Override
	protected ClassRealm createExtensionRealm(final ClassRealm plexusCore, final ClassRealm currentRealm)
			throws DuplicateRealmException {
		// we need to reinitiate the r4m-maven-extension realm because maven injects an
		// outdated version of the plexus-utils
		final ClassRealm realm = plexusCore
				.createChildRealm("extension>net.runeduniverse.tools.maven.r4m:r4m-maven-extension");
		realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m");
		realm.importFrom(currentRealm, "net.runeduniverse.lib.utils");
		return realm;
	}

	@Override
	protected boolean patchMaven(final MavenSession mvnSession, final boolean isCoreExt,
			final Set<Extension> extensions, final Set<Plugin> extPlugins) throws MavenExecutionException {
		this.extensions.addAll(extensions);

		try {
			for (MavenProject mvnProject : mvnSession.getAllProjects())
				scanProject(mvnSession, extPlugins, mvnProject);
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

		Set<Plugin> unidentifiablePlugins = this.unidentifiablePlugins.get(mvnProject);
		if (unidentifiablePlugins == null) {
			this.unidentifiablePlugins.put(mvnProject, unidentifiablePlugins = new LinkedHashSet<>());
		}

		mvnProject.getBuild()
				.getPlugins()
				.addAll(extPlugins);

		for (MavenProjectScanner scanner : this.mavenProjectScanner)
			scanner.scan(mvnSession, extPlugins, unidentifiablePlugins, mvnProject);
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

	private void modifyLifecycleExecutionPlanCalculator() {
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

	@Override
	protected void _patchEventInfo_PatchingStarted() {
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_PATCHING_STARTED));
	}

	@Override
	protected void _patchEventInfo_SwitchRealmToPlexus() {
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_ELEVATING_TO_PLEXUS_REALM));
	}

	@Override
	protected void _patchEventInfo_SwitchRealmToMavenExt() {
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_ELEVATING_TO_CORE_REALM));
	}

	@Override
	protected void _patchEventInfo_SwitchRealmToBuildExt() {
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_ELEVATING_TO_BUILD_REALM));
	}

	@Override
	protected void _patchEventError_PatchingAborted(MavenExecutionException ex) {
		this.dispatcher.onEvent(PatchingEvent.createErrorEvent(Type.INFO_PATCHING_ABORTED, ex));
	}

	@Override
	protected void _patchEventInfo_ResetRealm() {
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_RETURNING_TO_EXTENSION_REALM));
	}

	@Override
	protected void _patchEventInfo_UnidentifiablePluginsDetected(Collection<MavenProject> projects) {
		this.dispatcher.onEvent(MavenPluginPatchingEvent.createInfoEvent(Type.WARN_UNIDENTIFIABLE_PLUGIN_DETECTED,
				this.unidentifiablePlugins));
	}

	@Override
	protected void _patchEventInfo_PatchingFinished() {
		this.dispatcher.onEvent(PatchingEvent.createInfoEvent(Type.INFO_PATCHING_FINISHED));
	}
}
