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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.apache.maven.model.Build;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Model;
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

import net.runeduniverse.lib.utils.common.api.DataMap;
import net.runeduniverse.tools.maven.r4m.api.Settings;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSector;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSectorSnapshot;
import net.runeduniverse.tools.maven.r4m.pem.api.ModelPredicate;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelCompatProjectParser;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelOverrideFilterSupplier;
import net.runeduniverse.tools.maven.r4m.pem.model.DefaultModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelProperties;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetPhase;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ProjectExecutionModelCompatProjectParser.class, hint = ExecutionsPluginParser.HINT)
public class ExecutionsPluginParser implements ProjectExecutionModelCompatProjectParser {

	public static final String HINT = "plugin-execution";
	public static final String ERR_MSG_PLUGIN_DESCRIPTOR = "Failed to acquire org.apache.maven.plugin.descriptor.PluginDescriptor!";

	@Requirement
	protected Logger log;
	@Requirement
	protected MavenPluginManager manager;
	@Requirement(role = org.apache.maven.lifecycle.Lifecycle.class)
	protected Map<String, org.apache.maven.lifecycle.Lifecycle> mvnLifecycles;
	@Requirement
	protected Settings settings;
	@Requirement
	protected ExecutionArchive archive;
	@Requirement(role = ProjectExecutionModelOverrideFilterSupplier.class)
	protected Set<ProjectExecutionModelOverrideFilterSupplier> overrideFilterSupplier;

	@Override
	public ProjectExecutionModel parse(final Set<Plugin> invalidPlugins, final List<RemoteRepository> repositories,
			final RepositorySystemSession session, final MavenProject mvnProject) throws Exception {
		if (mvnProject == null)
			return null;
		// if maven backwards compat-mode is disabled, return null
		if (!this.settings.getMavenBackwardsCompatible()
				.getSelected())
			return null;

		// optain active overrides
		final Set<ModelPredicate<ProjectExecutionModel, Execution>> filterSet = getOverrideFilter(mvnProject);
		final List<Plugin> buildPlugins = new LinkedList<>(mvnProject.getBuildPlugins());

		final Map<String, Map<String, Execution>> managedPluginIndex = aggregateExecutions(invalidPlugins, repositories,
				session, filterSet, mvnProject, p -> {
					final Model originalModel = p.getOriginalModel();
					if (originalModel == null)
						return null;
					final Build buildModel = originalModel.getBuild();
					if (buildModel == null)
						return null;
					final PluginManagement pluginManagement = buildModel.getPluginManagement();
					if (pluginManagement == null)
						return null;
					return pluginManagement.getPlugins();
				});

		final Map<String, Map<String, Execution>> pluginIndex = aggregateExecutions(invalidPlugins, repositories,
				session, filterSet, mvnProject, p -> {
					final Model originalModel = p.getOriginalModel();
					if (originalModel == null)
						return null;
					final Build buildModel = originalModel.getBuild();
					if (buildModel == null)
						return null;
					return buildModel.getPlugins();
				});

		// --------------------------------------------------------------------

		final List<Profile> activeProfiles = mvnProject.getActiveProfiles();
		if (activeProfiles != null) {
			for (Profile activeProfile : activeProfiles) {
				if (activeProfile == null)
					continue;
				final String profileId = activeProfile.getId();
				if (isBlank(profileId))
					continue;
				// collect build plugins
				{
					final BuildBase buildModel = activeProfile.getBuild();
					if (buildModel != null)
						buildPlugins.addAll(buildModel.getPlugins());
				}

				final Map<String, Map<String, Execution>> profileManagedPluginIndex = aggregateExecutions(
						invalidPlugins, repositories, session, filterSet, mvnProject, p -> {
							final Model originalModel = p.getOriginalModel();
							if (originalModel == null)
								return null;

							for (Profile profile : originalModel.getProfiles()) {
								if (profile == null)
									continue;
								// find the correct plugin
								if (!profileId.equals(profile.getId()))
									continue;
								// get the plugin list
								final BuildBase buildModel = profile.getBuild();
								if (buildModel == null)
									return null;
								final PluginManagement pluginManagement = buildModel.getPluginManagement();
								if (pluginManagement == null)
									return null;
								return pluginManagement.getPlugins();
							}
							return null;
						});

				for (Entry<String, Map<String, Execution>> pluginEntry : profileManagedPluginIndex.entrySet()) {
					managedPluginIndex.computeIfAbsent(pluginEntry.getKey(), k -> new LinkedHashMap<>())
							.putAll(pluginEntry.getValue());
				}

				final Map<String, Map<String, Execution>> profilePluginIndex = aggregateExecutions(invalidPlugins,
						repositories, session, filterSet, mvnProject, p -> {
							final Model originalModel = p.getOriginalModel();
							if (originalModel == null)
								return null;

							for (Profile profile : originalModel.getProfiles()) {
								if (profile == null)
									continue;
								// find the correct plugin
								if (!profileId.equals(profile.getId()))
									continue;
								// get the plugin list
								final BuildBase buildModel = profile.getBuild();
								if (buildModel == null)
									return null;
								return buildModel.getPlugins();
							}
							return null;
						});

				for (Entry<String, Map<String, Execution>> pluginEntry : profilePluginIndex.entrySet()) {
					pluginIndex.computeIfAbsent(pluginEntry.getKey(), k -> new LinkedHashMap<>())
							.putAll(pluginEntry.getValue());
				}

			}
		}

		// --------------------------------------------------------------------

		final ProjectExecutionModel pem = createPem(mvnProject);
		pem.setInherited(false);

		for (Plugin mvnPlugin : buildPlugins) {
			final Map<String, Execution> managedMap = managedPluginIndex.remove(mvnPlugin.getKey());
			final Map<String, Execution> map = pluginIndex.get(mvnPlugin.getKey());

			if (map != null) {
				for (Entry<String, Execution> entry : map.entrySet()) {
					if (managedMap != null)
						managedMap.remove(entry.getKey());
					final Execution execution = entry.getValue();
					if (execution == null)
						continue;
					execution.setAlwaysActive(true);
					pem.addExecution(execution);
				}
			}
			if (managedMap != null) {
				for (Execution execution : managedMap.values()) {
					if (execution == null)
						continue;
					execution.setAlwaysActive(true);
					pem.addExecution(execution);
				}
			}
		}

		for (Map<String, Execution> map : managedPluginIndex.values()) {
			for (Execution exec : map.values()) {
				if (exec == null)
					continue;
				pem.addExecution(exec);
			}
		}

		return pem;
	}

	protected Set<ModelPredicate<ProjectExecutionModel, Execution>> getOverrideFilter(final MavenProject mvnProject) {
		final Set<ModelPredicate<ProjectExecutionModel, Execution>> set = new LinkedHashSet<>();
		final ExecutionArchiveSector sector = this.archive.getSector(mvnProject);
		if (sector == null)
			return set;
		final ExecutionArchiveSectorSnapshot snapshot = sector.snapshot();
		if (snapshot == null)
			return set;
		final DataMap<String, AtomicBoolean, ExecutionArchiveSectorSnapshot.Data> overrides = //
				snapshot.collectOverridesAsBooleanMapWithData();
		for (ProjectExecutionModelOverrideFilterSupplier supplier : this.overrideFilterSupplier) {
			final ModelPredicate<ProjectExecutionModel, Execution> filter = supplier.get(overrides);
			if (filter == null)
				continue;
			set.add(filter);
		}
		return set;
	}

	protected Map<String, Map<String, Execution>> aggregateExecutions(final Set<Plugin> invalidPlugins,
			final List<RemoteRepository> repositories, final RepositorySystemSession session,
			final Set<ModelPredicate<ProjectExecutionModel, Execution>> filterSet, final MavenProject rootMvnProject,
			final Function<MavenProject, Collection<Plugin>> pluginsSupplier) {
		Map<String, Map<String, Execution>> domMap = null;
		boolean requireInherited = false;

		for (MavenProject mvnProject = rootMvnProject; mvnProject != null; mvnProject = mvnProject.getParent()) {
			final ProjectExecutionModel pem = createPem(mvnProject);
			// baseMap = executions of mvnProject (parent)
			final Map<String, Map<String, Execution>> baseMap = //
					indexPlugins(invalidPlugins, repositories, session, rootMvnProject, mvnProject, pem,
							pluginsSupplier);
			// filter the baseMap
			for (Entry<String, Map<String, Execution>> pluginEntry : baseMap.entrySet()) {
				for (Iterator<Entry<String, Execution>> i = //
						pluginEntry.getValue()
								.entrySet()
								.iterator(); //
						i.hasNext(); //
						) {
					final Entry<String, Execution> entry = i.next();
					final Execution execution = entry.getValue();
					// check if it is inherited
					if (requireInherited && execution != null && !execution.isInherited()) {
						// if not than ensure that an upstream entry is removed!
						entry.setValue(null);
						continue;
					}
					for (ModelPredicate<ProjectExecutionModel, Execution> filter : filterSet) {
						if (!filter.test(pem, execution)) {
							i.remove();
							break;
						}
					}
				}
			}
			// merge
			if (domMap != null) {
				for (Entry<String, Map<String, Execution>> pluginEntry : domMap.entrySet()) {
					baseMap.computeIfAbsent(pluginEntry.getKey(), k -> new LinkedHashMap<>())
							.putAll(pluginEntry.getValue());
				}
			}
			domMap = baseMap;
			// for all parents require inherited executions
			requireInherited = true;
		}

		return domMap;
	}

	protected ProjectExecutionModel createPem(final MavenProject mvnProject) {
		final ProjectExecutionModel model = new ProjectExecutionModel();
		model.setModelSource(new DefaultModelSource() //
				.setFile(Paths.get("pom.xml"))
				.setNote("PEM derived from configured Executions"));
		model.setParser(ExecutionsPluginParser.class, ExecutionsPluginParser.HINT);
		model.setVersion(ModelProperties.MODEL_VERSION);
		final ModelSource source = model.computeModelSourceIfAbsent(DefaultModelSource::new);
		source.setProjectId(ModelSource.id(mvnProject::getGroupId, mvnProject::getArtifactId));
		return model;
	}

	protected Map<String, Map<String, Execution>> indexPlugins(final Set<Plugin> invalidPlugins,
			final List<RemoteRepository> repositories, final RepositorySystemSession session,
			final MavenProject rootMvnProject, final MavenProject mvnProject, final ProjectExecutionModel pem,
			final Function<MavenProject, Collection<Plugin>> pluginsSupplier) {
		final Map<String, Map<String, Execution>> pluginIndex = new LinkedHashMap<>();
		final Collection<Plugin> plugins = pluginsSupplier.apply(mvnProject);
		if (plugins == null)
			return pluginIndex;
		for (Plugin mvnPlugin : plugins) {
			mvnPlugin = resolvePlugin(rootMvnProject, mvnPlugin);
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
			pluginIndex.put(mvnPlugin.getKey(), convert(pem, mvnPluginDescriptor, mvnPlugin));
		}
		return pluginIndex;
	}

	protected Map<String, Execution> convert(final ProjectExecutionModel pem,
			final PluginDescriptor mvnPluginDescriptor, final Plugin mvnPlugin) {
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

			pem.addExecution(execution);
			executions.put(execution.getId(), execution);
		}
		return executions;
	}

	protected Plugin resolvePlugin(final MavenProject mvnProject, final Plugin plugin) {
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

	protected boolean isValid(final Set<Plugin> invalidPlugins, final Plugin mvnPlugin) {
		if (mvnPlugin == null)
			return false;

		if (mvnPlugin.getVersion() == null) {
			invalidPlugins.add(mvnPlugin);
			return false;
		}

		return true;
	}

	protected Goal createGoal(final Plugin mvnPlugin, final MojoDescriptor mvnMojoDescriptor) {
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

	protected Lifecycle acquireLifecycle(final Execution execution, final String phase) {
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
