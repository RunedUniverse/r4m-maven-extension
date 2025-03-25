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
package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;

public interface ExecutionFilterUtils {

	/**
	 * Creates the default {@link ExecutionFilter} for filtering out all the
	 * relevant Executions utilizing the provided
	 * {@link ExecutionArchiveSelectorConfig}.
	 *
	 * <p>
	 * Relevant are all Executions that are not permanently flagged as disabled and
	 * adhere to all restrictions.
	 *
	 * @param cnf the {@link ExecutionArchiveSelectorConfig} used for filtering in
	 *            the filter
	 * @return an instance of {@link ExecutionFilter} for filtering Executions by
	 *         relevance utilizing {@code cnf}
	 */
	public static Predicate<Execution> defaultRelevanceFilter(final ExecutionArchiveSelectorConfig cnf) {
		return execution -> {
			// the use of never-active flags is discouraged
			// and included for debugging purposes
			if (execution.isNeverActive())
				return false;
			// if restrictions are set at least one of each must match!
			if (!execution.getRestrictions()
					.isEmpty()) {
				final Map<String, Boolean> map = new LinkedHashMap<>();
				for (ExecutionRestriction<ExecutionArchiveSelectorConfig> restriction : execution
						.getRestrictions(ExecutionArchiveSelectorConfig.class)) {
					Boolean state = map.get(restriction.getHint());
					if (state != null && state)
						continue;
					map.put(restriction.getHint(), restriction.isActive(cnf));
				}
				if (map.containsValue(false))
					return false;
			}
			return true;
		};
	}

	/**
	 * Creates the default {@link ExecutionFilter} for filtering out all the active
	 * Executions utilizing the provided {@link ExecutionArchiveSelectorConfig}.
	 *
	 * <p>
	 * Active are all Executions that are not permanently flagged as disabled,
	 * adhere to all restrictions and are triggered by either a trigger or by direct
	 * invocation.
	 *
	 * @param cnf the {@link ExecutionArchiveSelectorConfig} used for filtering in
	 *            the filter
	 * @return an instance of {@link ExecutionFilter} for filtering for active
	 *         Executions utilizing {@code cnf}
	 */
	public static Predicate<Execution> defaultActiveFilter(final ExecutionArchiveSelectorConfig cnf) {
		return execution -> {
			// the use of never-active flags is discouraged
			// and included for debugging purposes
			if (execution.isNeverActive())
				return false;
			// if restrictions are set at least one of each must match!
			if (!execution.getRestrictions()
					.isEmpty()) {
				final Map<String, Boolean> map = new LinkedHashMap<>();
				for (ExecutionRestriction<ExecutionArchiveSelectorConfig> restriction : execution
						.getRestrictions(ExecutionArchiveSelectorConfig.class)) {
					Boolean state = map.get(restriction.getHint());
					if (state != null && state)
						continue;
					map.put(restriction.getHint(), restriction.isActive(cnf));
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
			for (ExecutionTrigger<ExecutionArchiveSelectorConfig> trigger : execution
					.getTrigger(ExecutionArchiveSelectorConfig.class))
				if (trigger.isActive(cnf))
					return true;
			return false;
		};
	}

}
