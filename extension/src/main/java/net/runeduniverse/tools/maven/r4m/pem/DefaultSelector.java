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
package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.MojoDescriptorCreator;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.lib.utils.common.api.DataMap;
import net.runeduniverse.lib.utils.maven3.ext.eventspy.api.EventSpyDispatcherProxy;
import net.runeduniverse.tools.maven.r4m.api.Settings;
import net.runeduniverse.tools.maven.r4m.event.api.ProjectExecutionModelOverrideDetectionEvent;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelector;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionRestrictionEvaluator;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionTriggerEvaluator;
import net.runeduniverse.tools.maven.r4m.pem.api.ModelPredicate;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelOverrideFilterSupplier;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSector;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSectorSnapshot;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.pem.view.DefaultPemViewFactory;
import net.runeduniverse.tools.maven.r4m.pem.view.api.ExecutionView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.GoalView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.LifecycleView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.PhaseView;

import static net.runeduniverse.tools.maven.r4m.pem.api.ExecutionFilterUtils.defaultActiveFilterSupplier;

@Component(role = ExecutionArchiveSelector.class, hint = "default", instantiationStrategy = "singleton")
public class DefaultSelector implements ExecutionArchiveSelector {

	public static final String WARN_SKIPPING_UNKNOWN_GOAL = "skipping unknown goal » %s:%s:%s";

	@Requirement
	protected Logger log;
	@Requirement
	protected Settings settings;
	@Requirement
	protected MavenSession mvnSession;
	@Requirement
	protected MavenPluginManager pluginManager;
	@Requirement
	protected MojoDescriptorCreator mojoDescriptorCreator;
	@Requirement
	protected ExecutionArchive archive;
	@Requirement
	protected EventSpyDispatcherProxy dispatcher;
	@Requirement(role = ProjectExecutionModelOverrideFilterSupplier.class)
	protected Set<ProjectExecutionModelOverrideFilterSupplier> overrideFilterSupplier;
	@Requirement
	protected ExecutionRestrictionEvaluator restrictionEvaluator;
	@Requirement
	protected ExecutionTriggerEvaluator triggerEvaluator;

	protected boolean validateGoal(final ExecutionArchiveSelectorConfig cnf, final Goal goal) {
		for (String mode : cnf.getModes())
			if (goal.getModes()
					.contains(mode))
				return true;
		return false;
	}

	protected boolean acquireMojoDescriptor(final ExecutionArchiveSelectorConfig cnf, final GoalView goalView,
			final boolean download) {
		final Plugin plugin = cnf.getActiveProject()
				.getPlugin(goalView.getGroupId() + ":" + goalView.getArtifactId());
		boolean loadManaged = true;

		if (plugin == null) {
			if (goalView.isOptional())
				return false;
			if (download)
				loadManaged = false;
			else
				return false;
		}

		final MojoDescriptor descriptor;
		try {
			if (loadManaged)
				descriptor = this.pluginManager.getMojoDescriptor(plugin, goalView.getGoalId(), cnf.getActiveProject()
						.getRemotePluginRepositories(), this.mvnSession.getRepositorySession());
			else
				descriptor = this.mojoDescriptorCreator.getMojoDescriptor(String.format("%s:%s:%s",
						goalView.getGroupId(), goalView.getArtifactId(), goalView.getGoalId()), this.mvnSession,
						cnf.getActiveProject());
		} catch (MojoNotFoundException | PluginResolutionException | PluginDescriptorParsingException
				| InvalidPluginDescriptorException e) {
			this.log.debug("Failed to acquire MojoDescriptor!", e);
			return false;
		} catch (PluginNotFoundException | NoPluginFoundForPrefixException | PluginVersionResolutionException e) {
			this.log.debug("Failed to acquire maven plugin!", e);
			return false;
		}
		if (descriptor == null)
			return false;

		goalView.setDescriptor(descriptor);
		return true;
	}

	protected Map<String, Map<ExecutionSource, ExecutionView>> aggregate(final ExecutionArchiveSelectorConfig cnf,
			final Collection<Execution> executions) {
		final boolean warn = "warn".equals(this.settings.getMissingBuildPluginHandler()
				.getSelected());
		final boolean download = "download".equals(this.settings.getMissingBuildPluginHandler()
				.getSelected());
		final Map<String, Map<ExecutionSource, ExecutionView>> map = new LinkedHashMap<>();

		for (Execution execution : executions) {
			final Map<ExecutionSource, ExecutionView> entry = map.computeIfAbsent(execution.getId(),
					k -> new LinkedHashMap<>());
			final ExecutionView executionView = entry.computeIfAbsent(execution.getSource(),
					k -> DefaultPemViewFactory.createExecution(execution.getId()));
			for (Lifecycle lifecycle : execution.getLifecycles()
					.values()) {
				final LifecycleView lifecycleView = executionView.computeLifecycleIfAbsent(lifecycle.getId(),
						DefaultPemViewFactory::createLifecycle);
				for (Phase phase : lifecycle.getPhases()
						.values()) {
					final PhaseView phaseView = lifecycleView.computePhaseIfAbsent(phase.getId(),
							DefaultPemViewFactory::createPhase);
					for (Goal goal : phase.getGoals())
						if (this.validateGoal(cnf, goal)) {
							final GoalView goalView = DefaultPemViewFactory.createGoal(goal.getGroupId(),
									goal.getArtifactId(), goal.getGoalId());
							goalView.addModes(goal.getModes());
							goalView.setOptional(goal.getOptional() != null && goal.getOptional());
							goalView.setFork(goal.getFork());
							if (acquireMojoDescriptor(cnf, goalView, download))
								phaseView.addGoal(goalView);
							else if (!goalView.isOptional() && (warn || download))
								this.log.warn(String.format(WARN_SKIPPING_UNKNOWN_GOAL, goalView.getGroupId(),
										goalView.getArtifactId(), goalView.getGoalId()));
						}
				}
			}
		}
		return map;
	}

	protected ExecutionView merge(final ExecutionArchiveSelectorConfig cnf, final ExecutionView base,
			final ExecutionView dominant, final boolean lifecycleOverride, final boolean goalOverride) {
		for (LifecycleView domLifecycle : dominant.getLifecycles()
				.values()) {
			final LifecycleView baseLifecycle = base.getLifecycles()
					.get(domLifecycle.getId());
			if (lifecycleOverride || baseLifecycle == null) {
				base.put(domLifecycle);
				continue;
			}

			// remove all dominant goals from base phases, which can only be executed once
			final Map<GoalView, PhaseView> basePhaseIndex = new HashMap<>();
			for (PhaseView basePhase : baseLifecycle.getPhases()
					.values())
				for (GoalView goal : basePhase.getGoals())
					basePhaseIndex.put(goal, basePhase);

			for (PhaseView domPhase : domLifecycle.getPhases()
					.values())
				for (GoalView domGoal : domPhase.getGoals())
					if (goalOverride || !domGoal.getDescriptor()
							.alwaysExecute())
						for (GoalView baseGoal : basePhaseIndex.keySet()) {
							if (domGoal.equals(baseGoal))
								basePhaseIndex.get(baseGoal)
										.removeGoal(baseGoal);
						}

			// push all dominant phases and goals into base lifecycles
			for (PhaseView domPhase : domLifecycle.getPhases()
					.values()) {
				final PhaseView basePhase = baseLifecycle.getPhases()
						.get(domPhase.getId());
				if (basePhase == null) {
					baseLifecycle.put(domPhase);
					continue;
				}
				basePhase.addNewGoals(domPhase.getGoals());
			}
		}
		return base;
	}

	protected Map<String, Map<ExecutionSource, ExecutionView>> getExecutions(final ExecutionArchiveSelectorConfig cnf,
			final ExecutionArchiveSectorSnapshot snapshot) {
		final Map<String, Map<ExecutionSource, ExecutionView>> views = new LinkedHashMap<>();
		final Map<String, AtomicBoolean> overrides = snapshot.collectOverridesAsBooleanMap();

		getExecutions(cnf, defaultActiveFilterSupplier(this.restrictionEvaluator, this.triggerEvaluator, cnf), views,
				snapshot, overrides, false);

		final DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> overridesHinted = snapshot
				.collectOverridesAsHintedBooleanMapWithModels();
		final Set<ProjectExecutionModel> overrideModelIndex = new LinkedHashSet<>();
		overridesHinted.forEach((k, b, models) -> {
			if (models == null)
				return;
			overrideModelIndex.addAll(models);
		});
		this.dispatcher.onEvent(ProjectExecutionModelOverrideDetectionEvent.createEvent(cnf.getTopLevelProject(),
				cnf.getActiveProject(), overridesHinted.toValueMap(), overrideModelIndex));
		return views;
	}

	protected boolean getExecutions(final ExecutionArchiveSelectorConfig cnf,
			final ModelPredicate<ProjectExecutionModel, Execution> filter,
			final Map<String, Map<ExecutionSource, ExecutionView>> baseViews,
			final ExecutionArchiveSectorSnapshot snapshot, final Map<String, AtomicBoolean> overrides,
			final boolean requireInherited) {
		snapshot.applyOverrides(overrides, this.overrideFilterSupplier);

		Set<Execution> applicableExecutions = snapshot.getEffectiveExecutions(filter, requireInherited);
		boolean effExecDetected = snapshot.hasModelWithEffectiveOverride();

		if (!effExecDetected && snapshot.getParent() != null)
			effExecDetected = getExecutions(cnf, filter, baseViews, snapshot.getParent(), overrides, true);
		if (!effExecDetected)
			applicableExecutions = snapshot.getExecutions(filter, requireInherited);

		applicableExecutions.addAll(snapshot.getUserDefinedExecutions(filter, requireInherited));

		integrateExecutions(cnf, baseViews, applicableExecutions);

		return effExecDetected;
	}

	public static boolean checkActiveOverrides(final Map<String, AtomicBoolean> overrides,
			final ProjectExecutionModel model) {
		if (overrides == null || overrides.isEmpty() || model == null)
			return false;

		for (Entry<String, ModelOverride> entry : model.getOverridesAsMap()
				.entrySet()) {
			final AtomicBoolean value = overrides.get(entry.getKey());
			final ModelOverride override = entry.getValue();
			if (value == null || override == null)
				continue;
			if (value.get() == override.isActive())
				return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	protected void integrateExecutions(final ExecutionArchiveSelectorConfig cnf,
			final Map<String, Map<ExecutionSource, ExecutionView>> baseViews, final Set<Execution> executions) {
		final Map<String, Map<ExecutionSource, ExecutionView>> dominantViews = aggregate(cnf, executions);

		for (String executionId : dominantViews.keySet()) {
			final Map<ExecutionSource, ExecutionView> baseEntries = baseViews.get(executionId);
			final Map<ExecutionSource, ExecutionView> domEntries = dominantViews.get(executionId);
			if (baseEntries == null) {
				baseViews.put(executionId, domEntries);
				continue;
			}
			baseEntries.remove(ExecutionSource.EFFECTIVE);
			for (ExecutionSource source : domEntries.keySet()) {
				final ExecutionView baseExecution = baseEntries.get(source);
				final ExecutionView domExecution = domEntries.get(source);
				if (baseExecution == null || ExecutionSource.EFFECTIVE.equals(source)) {
					baseEntries.put(source, domExecution);
					continue;
				}
				baseEntries.put(source,
						merge(cnf, baseExecution, domExecution, ExecutionSource.OVERRIDE.equals(source), false));
			}
		}
	}

	@SuppressWarnings("deprecation")
	protected ExecutionView reduce(final ExecutionArchiveSelectorConfig cnf, final String id,
			final Map<ExecutionSource, ExecutionView> views) {
		ExecutionView execution = views.get(ExecutionSource.EFFECTIVE);
		if (execution != null)
			return execution;

		final ExecutionView workflowView = views.getOrDefault(ExecutionSource.WORKFLOW,
				DefaultPemViewFactory.createExecution(id));
		final ExecutionView packagingView = views.getOrDefault(ExecutionSource.PACKAGING,
				DefaultPemViewFactory.createExecution(id));

		execution = merge(cnf, packagingView, workflowView, true, false);

		final ExecutionView pluginView = views.getOrDefault(ExecutionSource.PLUGIN,
				DefaultPemViewFactory.createExecution(id));
		final ExecutionView overrideView = views.getOrDefault(ExecutionSource.OVERRIDE,
				DefaultPemViewFactory.createExecution(id));

		execution = merge(cnf, pluginView, execution, false, true);
		execution = merge(cnf, execution, overrideView, true, false);

		return execution;
	}

	@Override
	public ExecutionArchiveSelection compileSelection(final ExecutionArchiveSelectorConfig selectorConfig) {
		final Set<ExecutionView> views = new LinkedHashSet<>();
		if (selectorConfig.getActiveProject() == null)
			return new DefaultSelection(selectorConfig.clone(), views);

		final ExecutionArchiveSector sector = this.archive.getSector(selectorConfig.getActiveProject());
		if (sector == null)
			return new DefaultSelection(selectorConfig.clone(), views);

		final ExecutionArchiveSectorSnapshot snapshot = sector.snapshot();
		selectorConfig.compile(this.mvnSession);
		for (Entry<String, Map<ExecutionSource, ExecutionView>> entry : getExecutions(selectorConfig, snapshot)
				.entrySet())
			views.add(reduce(selectorConfig, entry.getKey(), entry.getValue()));
		return new DefaultSelection(selectorConfig.clone(), views);
	}

}
