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
package net.runeduniverse.tools.maven.r4m.pem.parser;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.apache.maven.model.Build;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Profile;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelPomParser;
import net.runeduniverse.tools.maven.r4m.pem.model.DefaultModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelProperties;
import net.runeduniverse.tools.maven.r4m.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetPhase;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ProjectExecutionModelPomParser.class, hint = ExecutionsPluginParser.HINT)
public class ExecutionsPluginParser implements ProjectExecutionModelPomParser {

	public static final String HINT = "plugin-execution";
	public static final String ERR_MSG_PLUGIN_DESCRIPTOR = "Failed to acquire org.apache.maven.plugin.descriptor.PluginDescriptor!";

	@Requirement
	protected Logger log;
	@Requirement
	protected MavenPluginManager manager;
	@Requirement(role = org.apache.maven.lifecycle.Lifecycle.class)
	private Map<String, org.apache.maven.lifecycle.Lifecycle> mvnLifecycles;

	@Override
	public ProjectExecutionModel parse(final Set<Plugin> invalidPlugins, final List<RemoteRepository> repositories,
			final RepositorySystemSession session, final MavenProject mvnProject, final List<Profile> profiles,
			final Build buildModel) throws Exception {
		System.out.println("--- " + mvnProject.getId());

		final ProjectExecutionModel model = new ProjectExecutionModel();
		model.setModelSource(new DefaultModelSource() //
				.setFile(Paths.get("pom.xml"))
				.setNote("PEM derived from configured Executions"));
		model.setParser(ExecutionsPluginParser.class, ExecutionsPluginParser.HINT);
		model.setVersion(ModelProperties.MODEL_VERSION);

		final Map<String, Map<String, Execution>> managedPluginIndex = new LinkedHashMap<>();
		final Map<String, Map<String, Execution>> pluginIndex = new LinkedHashMap<>();

		if (buildModel != null) {
			final PluginManagement pluginManagement = buildModel.getPluginManagement();
			if (pluginManagement != null) {
				indexPlugins(invalidPlugins, repositories, session, mvnProject, managedPluginIndex,
						pluginManagement.getPlugins());
			}
			indexPlugins(invalidPlugins, repositories, session, mvnProject, pluginIndex, buildModel.getPlugins());
		}

		final Map<String, Profile> profileMap = new LinkedHashMap<>();
		for (Profile profile : profiles) {
			if (profile == null)
				continue;
			profileMap.put(profile.getId(), profile);
		}

		final List<Profile> activeProfiles = mvnProject.getActiveProfiles();
		if (activeProfiles != null) {
			for (Profile activeProfile : activeProfiles) {
				if (activeProfile == null)
					continue;
				final Profile profile = profileMap.get(activeProfile.getId());
				if (profile == null)
					continue;
				final BuildBase build = profile.getBuild();
				if (build == null)
					continue;

				final PluginManagement pluginManagement = build.getPluginManagement();
				if (pluginManagement != null) {
					indexPlugins(invalidPlugins, repositories, session, mvnProject, managedPluginIndex,
							pluginManagement.getPlugins());
				}
				indexPlugins(invalidPlugins, repositories, session, mvnProject, pluginIndex, build.getPlugins());
			}
		}

		// TODO archive managedPluginIndex & pluginIndex at this point!

		for (Plugin mvnPlugin : mvnProject.getBuildPlugins()) {
			final Map<String, Execution> managedMap = managedPluginIndex.remove(mvnPlugin.getKey());
			final Map<String, Execution> map = pluginIndex.get(mvnPlugin.getKey());

			if (map != null) {
				for (Entry<String, Execution> entry : map.entrySet()) {
					if (managedMap != null)
						managedMap.remove(entry.getKey());
					final Execution execution = entry.getValue();
					execution.setAlwaysActive(true);
					model.addExecution(execution);
				}
			}
			if (managedMap != null) {
				for (Execution execution : managedMap.values()) {
					execution.setAlwaysActive(true);
					model.addExecution(execution);
				}
			}
		}

		for (Map<String, Execution> map : managedPluginIndex.values()) {
			model.addExecutions(map.values());
		}

		return model;
	}

	private void indexPlugins(final Set<Plugin> invalidPlugins, final List<RemoteRepository> repositories,
			final RepositorySystemSession session, final MavenProject mvnProject,
			final Map<String, Map<String, Execution>> pluginIndex, final Collection<Plugin> plugins) {
		for (Plugin mvnPlugin : plugins) {
			mvnPlugin = resolvePlugin(mvnProject, mvnPlugin);
			if (!isValid(invalidPlugins, mvnPlugin))
				continue;

			final PluginDescriptor mvnPluginDescriptor;
			try {
				mvnPluginDescriptor = this.manager.getPluginDescriptor(mvnPlugin, repositories, session);
			} catch (PluginResolutionException | PluginDescriptorParsingException
					| InvalidPluginDescriptorException e) {
				this.log.error(ERR_MSG_PLUGIN_DESCRIPTOR, e);
				continue;
			}
			pluginIndex.put(mvnPlugin.getKey(), convert(mvnPluginDescriptor, mvnPlugin));
		}
	}

	private Map<String, Execution> convert(final PluginDescriptor mvnPluginDescriptor, final Plugin mvnPlugin) {
		System.out.println(mvnPlugin.getId());

		final Map<String, Execution> executions = new LinkedHashMap<>();
		for (PluginExecution mvnExecution : mvnPlugin.getExecutions()) {
			final Execution execution = new Execution(mvnExecution.getId(), ExecutionSource.OVERRIDE);
			execution.setInherited(mvnExecution.isInherited());

			if (mvnExecution.getPhase() != null) {
				final Lifecycle lifecycle = acquireLifecycle(execution, mvnExecution.getPhase());
				if (lifecycle == null)
					continue;
				final Phase phase = lifecycle.computePhaseIfAbsent(mvnExecution.getPhase(), Phase::new);
				for (String goalId : mvnExecution.getGoals()) {
					final MojoDescriptor mvnMojoDescriptor = mvnPluginDescriptor.getMojo(goalId);
					if (mvnMojoDescriptor == null)
						continue;
					final Goal goal = createGoal(mvnPlugin, mvnMojoDescriptor);
					if (goal != null)
						phase.addGoal(goal);
				}
				if (phase.getGoals()
						.isEmpty())
					continue;
				lifecycle.putPhase(phase);
				execution.putLifecycle(lifecycle);
			} else
				for (String goalId : mvnExecution.getGoals()) {
					final MojoDescriptor mvnMojoDescriptor = mvnPluginDescriptor.getMojo(goalId);
					if (mvnMojoDescriptor == null)
						continue;
					final String phaseId = mvnMojoDescriptor.getPhase();
					if (isBlank(phaseId))
						continue;
					final Lifecycle lifecycle = acquireLifecycle(execution, phaseId);
					if (lifecycle == null)
						continue;
					final Phase phase = lifecycle.computePhaseIfAbsent(phaseId, Phase::new);
					final Goal goal = createGoal(mvnPlugin, mvnMojoDescriptor);
					if (goal == null)
						continue;
					phase.addGoal(goal);
					lifecycle.putPhase(phase);
					execution.putLifecycle(lifecycle);
				}

			if (execution.getLifecycles()
					.isEmpty())
				continue;

			System.out.println("  exec: " + execution.getId());
			executions.put(execution.getId(), execution);
		}
		return executions;
	}

	private Plugin resolvePlugin(final MavenProject mvnProject, final Plugin plugin) {
		if (plugin == null)
			return null;

		final Plugin clone;
		try {
			clone = plugin.clone();
		} catch (RuntimeException e) {
			log.error("Failed to clone Plugin!", e);
			return plugin;
		}

		// dont trust the currently set version!
		String version = null;
		final String key = plugin.getKey();
		Plugin ref = mvnProject.getPlugin(key);
		if (ref != null) {
			version = ref.getVersion();
			if (!isBlank(version)) {
				clone.setVersion(version);
				return clone;
			}
		}

		final PluginManagement pluginMgmtModel = mvnProject.getPluginManagement();
		if (pluginMgmtModel == null)
			return plugin;
		ref = pluginMgmtModel.getPluginsAsMap()
				.get(key);
		if (ref != null) {
			version = ref.getVersion();
			if (!isBlank(version)) {
				clone.setVersion(version);
				return clone;
			}
		}

		return plugin;
	}

	private boolean isValid(final Set<Plugin> invalidPlugins, final Plugin mvnPlugin) {
		if (mvnPlugin == null)
			return false;

		if (mvnPlugin.getVersion() == null) {
			invalidPlugins.add(mvnPlugin);
			return false;
		}

		return true;
	}

	private Goal createGoal(final Plugin mvnPlugin, final MojoDescriptor mvnMojoDescriptor) {
		final Goal goal = new Goal(mvnPlugin.getGroupId(), mvnPlugin.getArtifactId(), mvnMojoDescriptor.getGoal())
				.addModes("default", "dev");
		final Fork fork = new Fork();

		final String executeGoal = mvnMojoDescriptor.getExecuteGoal();
		if (!isBlank(executeGoal)) {
			// TODO maybe add this as a feature?
			/*
			 * anyway the goal gets forked as long as no phases (or lifecycles) get defined
			 * through {@link MojoDescriptor}, PEM or other means
			 *
			 * (extension version: >1.1.0)
			 */
		}

		final String executePhase = mvnMojoDescriptor.getExecutePhase();
		if (!isBlank(executePhase)) {
			fork.addPhase(new TargetPhase(executePhase));
		}
		final String executeLifecycle = mvnMojoDescriptor.getExecuteLifecycle();
		if (!isBlank(executeLifecycle))
			fork.setLifecycle(new TargetLifecycle(executeLifecycle));

		if (fork.isValid())
			goal.setFork(fork);
		return goal;
	}

	private Lifecycle acquireLifecycle(final Execution execution, final String phase) {
		String id = null;
		for (Entry<String, org.apache.maven.lifecycle.Lifecycle> entry : this.mvnLifecycles.entrySet())
			if (entry.getValue()
					.getPhases()
					.contains(phase)) {
				id = entry.getKey();
				break;
			}
		if (id == null)
			return null;
		final Lifecycle lifecycle = execution.getLifecycle(id);
		if (lifecycle != null)
			return lifecycle;
		return new Lifecycle(id);
	}
}
