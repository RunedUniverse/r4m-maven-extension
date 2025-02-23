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
package net.runeduniverse.tools.maven.r4m.pem;

import static net.runeduniverse.tools.maven.r4m.pem.api.ExecutionFilterUtils.defaultActiveFilter;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
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

import net.runeduniverse.tools.maven.r4m.api.Settings;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelector;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSector;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionFilter;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.pem.view.ViewFactory;
import net.runeduniverse.tools.maven.r4m.pem.view.api.ExecutionView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.GoalView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.LifecycleView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.PhaseView;;

@Component(role = ExecutionArchiveSelector.class, hint = "default", instantiationStrategy = "singleton")
public class Selector implements ExecutionArchiveSelector {

	public static final String WARN_SKIPPING_UNKNOWN_GOAL = "skipping unknown goal » %s:%s:%s";

	@Requirement
	private Logger log;
	@Requirement
	private Settings settings;
	@Requirement
	private MavenSession mvnSession;
	@Requirement
	private MavenPluginManager pluginManager;
	@Requirement
	private MojoDescriptorCreator mojoDescriptorCreator;
	@Requirement
	private ExecutionArchive archive;

	protected boolean validateGoal(final ExecutionArchiveSelectorConfig cnf, Goal goal) {
		for (String mode : cnf.getModes())
			if (goal.getModes()
					.contains(mode))
				return true;
		return false;
	}

	protected boolean acquireMojoDescriptor(final ExecutionArchiveSelectorConfig cnf, GoalView goalView,
			boolean download) {
		Plugin plugin = cnf.getActiveProject()
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

		MojoDescriptor descriptor = null;
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
			Collection<Execution> executions) {
		boolean warn = "warn".equals(this.settings.getMissingBuildPluginHandler()
				.getSelected());
		boolean download = "download".equals(this.settings.getMissingBuildPluginHandler()
				.getSelected());
		Map<String, Map<ExecutionSource, ExecutionView>> map = new LinkedHashMap<>();

		for (Execution execution : executions) {
			Map<ExecutionSource, ExecutionView> entry = map.get(execution.getId());
			if (entry == null) {
				entry = new LinkedHashMap<>();
				map.put(execution.getId(), entry);
			}
			ExecutionView executionView = entry.get(execution.getSource());
			if (executionView == null) {
				executionView = ViewFactory.createExecution(execution.getId());
				entry.put(execution.getSource(), executionView);
			}
			for (Lifecycle lifecycle : execution.getLifecycles()
					.values()) {
				LifecycleView lifecycleView = executionView.getLifecycle(lifecycle.getId());
				if (lifecycleView == null) {
					lifecycleView = ViewFactory.createLifecycle(lifecycle.getId());
					executionView.put(lifecycleView);
				}
				for (Phase phase : lifecycle.getPhases()
						.values()) {
					PhaseView phaseView = lifecycleView.getPhase(phase.getId());
					if (phaseView == null) {
						phaseView = ViewFactory.createPhase(phase.getId());
						lifecycleView.put(phaseView);
					}
					for (Goal goal : phase.getGoals())
						if (this.validateGoal(cnf, goal)) {
							GoalView goalView = ViewFactory.createGoal(goal.getGroupId(), goal.getArtifactId(),
									goal.getGoalId());
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
			final ExecutionView dominant, boolean lifecycleOverride, boolean goalOverride) {
		for (LifecycleView domLifecycle : dominant.getLifecycles()
				.values()) {
			LifecycleView baseLifecycle = base.getLifecycles()
					.get(domLifecycle.getId());
			if (lifecycleOverride || baseLifecycle == null) {
				base.put(domLifecycle);
				continue;
			}

			// remove all dominant goals from base phases, which can only be executed once
			Map<GoalView, PhaseView> basePhaseIndex = new HashMap<>();
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
				PhaseView basePhase = baseLifecycle.getPhases()
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
			final ExecutionArchiveSector slice) {
		Map<String, Map<ExecutionSource, ExecutionView>> views = new LinkedHashMap<>();
		getExecutions(cnf, defaultActiveFilter(cnf), views, slice, false);
		return views;
	}

	@SuppressWarnings("deprecation")
	protected boolean getExecutions(final ExecutionArchiveSelectorConfig cnf, final ExecutionFilter filter,
			final Map<String, Map<ExecutionSource, ExecutionView>> baseViews, final ExecutionArchiveSector sector,
			final boolean onlyInherited) {
		Set<Execution> applicableExecutions = sector.getEffectiveExecutions(filter, onlyInherited);
		boolean effExecDetected = false;

		if (applicableExecutions.isEmpty()) {
			if (sector.getParent() != null)
				effExecDetected = getExecutions(cnf, filter, baseViews, sector.getParent(), true);

			if (!effExecDetected)
				applicableExecutions = sector.getExecutions(filter, onlyInherited);
		} else
			effExecDetected = true;

		Map<String, Map<ExecutionSource, ExecutionView>> dominantViews = aggregate(cnf, applicableExecutions);

		for (String executionId : dominantViews.keySet()) {
			Map<ExecutionSource, ExecutionView> baseEntries = baseViews.get(executionId);
			Map<ExecutionSource, ExecutionView> domEntries = dominantViews.get(executionId);
			if (baseEntries == null) {
				baseViews.put(executionId, domEntries);
				continue;
			}
			baseEntries.remove(ExecutionSource.EFFECTIVE);
			for (ExecutionSource source : domEntries.keySet()) {
				ExecutionView baseExecution = baseEntries.get(source);
				ExecutionView domExecution = domEntries.get(source);
				if (baseExecution == null || ExecutionSource.EFFECTIVE.equals(source)) {
					baseEntries.put(source, domExecution);
					continue;
				}
				baseEntries.put(source,
						merge(cnf, baseExecution, domExecution, ExecutionSource.OVERRIDE.equals(source), false));
			}
		}

		return effExecDetected;
	}

	@SuppressWarnings("deprecation")
	protected ExecutionView reduce(final ExecutionArchiveSelectorConfig cnf, String id,
			Map<ExecutionSource, ExecutionView> views) {
		ExecutionView execution = views.get(ExecutionSource.EFFECTIVE);
		if (execution != null)
			return execution;

		ExecutionView workflowView = views.getOrDefault(ExecutionSource.WORKFLOW, ViewFactory.createExecution(id));
		ExecutionView packagingView = views.getOrDefault(ExecutionSource.PACKAGING, ViewFactory.createExecution(id));

		execution = merge(cnf, packagingView, workflowView, true, false);

		ExecutionView pluginView = views.getOrDefault(ExecutionSource.PLUGIN, ViewFactory.createExecution(id));
		ExecutionView overrideView = views.getOrDefault(ExecutionSource.OVERRIDE, ViewFactory.createExecution(id));

		execution = merge(cnf, pluginView, execution, false, true);
		execution = merge(cnf, execution, overrideView, true, false);

		return execution;
	}

	@Override
	public ExecutionArchiveSelection compileSelection(final ExecutionArchiveSelectorConfig selectorConfig) {
		Set<ExecutionView> views = new LinkedHashSet<>();
		if (selectorConfig.getActiveProject() == null)
			return new Selection(selectorConfig.clone(), views);

		ExecutionArchiveSector sector = this.archive.getSector(selectorConfig.getActiveProject());
		if (sector == null)
			return new Selection(selectorConfig.clone(), views);

		selectorConfig.compile(this.mvnSession);
		for (Entry<String, Map<ExecutionSource, ExecutionView>> entry : getExecutions(selectorConfig, sector)
				.entrySet())
			views.add(reduce(selectorConfig, entry.getKey(), entry.getValue()));
		return new Selection(selectorConfig.clone(), views);
	}

}
