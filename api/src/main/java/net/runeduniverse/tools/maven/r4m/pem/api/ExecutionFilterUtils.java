/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.runeduniverse.tools.maven.r4m.pem.model.DeclareSuperPemOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

import static net.runeduniverse.lib.utils.common.ComparisonUtils.typeIsAssignable;
import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

public interface ExecutionFilterUtils {

	/**
	 * Creates the default {@link ModelPredicate} for filtering out all the relevant
	 * Executions utilizing the provided {@link ExecutionArchiveSelectorConfig}.
	 *
	 * <p>
	 * Relevant are all Executions that are not permanently flagged as disabled and
	 * adhere to all restrictions.
	 *
	 * @param restrictionEvaluator evaluates {@link ExecutionRestriction} instances
	 * @param cnf                  the {@link ExecutionArchiveSelectorConfig} used
	 *                             for filtering in the filter
	 * @return an instance of {@link ModelPredicate} for filtering Executions by
	 *         relevance utilizing {@code cnf}
	 */
	public static ModelPredicate<ProjectExecutionModel, Execution> defaultRelevanceFilterSupplier(
			final ExecutionRestrictionEvaluator restrictionEvaluator, final ExecutionArchiveSelectorConfig cnf) {
		return (pem, execution) -> {
			// the use of never-active flags is discouraged
			// and included for debugging purposes
			if (execution.isNeverActive())
				return false;
			// if restrictions are set at least one of each must match!
			if (!execution.getRestrictions()
					.isEmpty()) {
				final Map<String, Boolean> map = new LinkedHashMap<>();
				for (ExecutionRestriction restriction : execution.getRestrictions()) {
					final Boolean state = map.get(restriction.type());
					if (state != null && state)
						continue;
					map.put(restriction.type(), restrictionEvaluator.isActive(cnf, restriction));
				}
				if (map.containsValue(false))
					return false;
			}
			return true;
		};
	}

	/**
	 * Creates the default {@link ModelPredicate} for filtering out all the active
	 * Executions utilizing the provided {@link ExecutionArchiveSelectorConfig}.
	 *
	 * <p>
	 * Active are all Executions that are not permanently flagged as disabled,
	 * adhere to all restrictions and are triggered by either a trigger or by direct
	 * invocation.
	 *
	 * @param restrictionEvaluator evaluates {@link ExecutionRestriction} instances
	 * @param triggerEvaluator     evaluates {@link ExecutionTrigger} instances
	 * @param cnf                  the {@link ExecutionArchiveSelectorConfig} used
	 *                             for filtering in the filter
	 * @return an instance of {@link ModelPredicate} for filtering for active
	 *         Executions utilizing {@code cnf}
	 */
	public static ModelPredicate<ProjectExecutionModel, Execution> defaultActiveFilterSupplier(
			final ExecutionRestrictionEvaluator restrictionEvaluator, final ExecutionTriggerEvaluator triggerEvaluator,
			final ExecutionArchiveSelectorConfig cnf) {
		return (pem, execution) -> {
			// the use of never-active flags is discouraged
			// and included for debugging purposes
			if (execution.isNeverActive())
				return false;
			// if restrictions are set at least one of each must match!
			if (!execution.getRestrictions()
					.isEmpty()) {
				final Map<String, Boolean> map = new LinkedHashMap<>();
				for (ExecutionRestriction restriction : execution.getRestrictions()) {
					final Boolean state = map.get(restriction.type());
					if (state != null && state)
						continue;
					map.put(restriction.type(), restrictionEvaluator.isActive(cnf, restriction));
				}
				if (map.containsValue(false))
					return false;
			}
			// the use of always-active flags is discouraged
			// and included for debugging purposes
			if (execution.isAlwaysActive())
				return true;
			// if an active-execution is defined it must match
			// if not the default-active flag is checked
			if (cnf.getActiveExecutions()
					.isEmpty()) {
				if (execution.isDefaultActive())
					return true;
			} else if (cnf.getActiveExecutions()
					.contains(execution.getId()))
				return true;
			// any active trigger activates the execution
			for (ExecutionTrigger trigger : execution.getTrigger()) {
				if (triggerEvaluator.isActive(cnf, trigger))
					return true;
			}
			return false;
		};
	}

	public static boolean requireInheritedFilter(final ProjectExecutionModel pem, final Execution execution) {
		return pem != null && pem.isInherited() //
				&& execution != null && execution.isInherited();
	}

	public static boolean requireUserDefinedFilter(final ProjectExecutionModel pem, final Execution execution) {
		return pem != null && pem.isUserDefined();
	}

	public static boolean requireSuperPemFilter(final ProjectExecutionModel pem, final Execution e) {
		if (pem == null)
			return false;

		final ModelOverride override = pem.getOverridesAsMap()
				.get(DeclareSuperPemOverride.TYPE);
		return override != null && override.isActive();
	}

	public static boolean disableSuperPomFilter(final ProjectExecutionModel pem, final Execution exec) {
		if (pem == null)
			return false;

		if (typeIsAssignable(ProjectExecutionModelPackagingParser.class, pem.getParserType())
				&& "default".equals(pem.getParserHint()))
			return false;

		// later checks require the Execution
		if (exec == null)
			return true;
		if (typeIsAssignable(ProjectExecutionModelPluginParser.class, pem.getParserType())
				&& "plugin-execution".equals(pem.getParserHint())) {

			final Map<String, Lifecycle> lifecycles = exec.getLifecycles();
			final String execId = exec.getId();
			// it has to start with 'default-'
			if (execId == null || !execId.startsWith("default-") //
					|| lifecycles == null)
				return true;

			// there must only be 1 goalId
			String goalId = null;
			for (Lifecycle lifecycle : lifecycles.values()) {
				if (lifecycle == null)
					continue;
				final Map<String, Phase> phases = lifecycle.getPhases();
				if (phases == null)
					continue;
				for (Phase phase : phases.values()) {
					if (phase == null)
						continue;
					final List<Goal> goals = phase.getGoals();
					for (Goal goal : goals) {
						if (goal == null)
							continue;
						final String id = goal.getGoalId();
						if (isBlank(id))
							continue;
						if (goalId != null)
							return true;
						goalId = id;
					}
				}
			}
			// goalId must not be null
			if (goalId == null)
				return true;

			// executionId must be 'default-' + goalId
			if (execId.equals("default-" + goalId))
				return false;
		}
		return true;
	}

}
