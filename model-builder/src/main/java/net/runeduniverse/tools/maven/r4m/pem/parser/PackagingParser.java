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
package net.runeduniverse.tools.maven.r4m.pem.parser;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.apache.maven.lifecycle.mapping.LifecycleMojo;
import org.apache.maven.lifecycle.mapping.LifecyclePhase;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.lib.utils.maven.MavenProperties;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelPackagingParser;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelProperties;
import net.runeduniverse.tools.maven.r4m.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.pem.restrictions.PackagingProcedureRestriction;

@Component(role = ProjectExecutionModelPackagingParser.class, hint = PackagingParser.HINT)
public class PackagingParser implements ProjectExecutionModelPackagingParser {

	public static final String HINT = "default";

	@Requirement
	protected Logger log;
	@Requirement(role = org.apache.maven.lifecycle.Lifecycle.class)
	protected Map<String, org.apache.maven.lifecycle.Lifecycle> defaultLifecycles;
	@Requirement(role = LifecycleMapping.class)
	protected Map<String, LifecycleMapping> mappings;

	@Override
	public ProjectExecutionModel parse() {
		final Set<Execution> effExecutions = new LinkedHashSet<>();

		for (Entry<String, LifecycleMapping> lifecycleMappingEntry : this.mappings.entrySet()) {
			final Set<String> allLifecycleIds = new HashSet<>(this.defaultLifecycles.keySet());
			final Map<String, Execution> executions = new LinkedHashMap<>();
			for (org.apache.maven.lifecycle.mapping.Lifecycle lifecycleMapping : lifecycleMappingEntry.getValue()
					.getLifecycles()
					.values()) {
				allLifecycleIds.remove(lifecycleMapping.getId());
				parse(executions, lifecycleMappingEntry.getKey(), lifecycleMapping.getId(),
						lifecycleMapping.getLifecyclePhases());
			}
			for (String lifecycleId : allLifecycleIds)
				parse(executions, lifecycleMappingEntry.getKey(), lifecycleId, this.defaultLifecycles.get(lifecycleId)
						.getDefaultLifecyclePhases());
			effExecutions.addAll(executions.values());
		}

		final ProjectExecutionModel model = new ProjectExecutionModel();
		model.setParser(PackagingParser.class, PackagingParser.HINT);
		model.setVersion(ModelProperties.MODEL_VERSION);
		model.addExecutions(effExecutions);
		return model;
	}

	protected void parse(final Map<String, Execution> executions, final String packagingProcedure,
			final String lifecycleId, final Map<String, LifecyclePhase> lifecyclePhases) {
		if (lifecyclePhases == null)
			return;
		for (Entry<String, LifecyclePhase> phaseMappingEntry : lifecyclePhases.entrySet()) {

			for (LifecycleMojo mojoMapping : phaseMappingEntry.getValue()
					.getMojos()) {
				Goal goal = new Goal();
				if (!goal.parseMvnGoalKey(mojoMapping.getGoal())) {
					// invalid goal!
					continue;
				}
				String executionId = String.join("-", MavenProperties.DEFAULT_EXECUTION_PREFIX, goal.getGoalId());
				Execution execution = executions.get(executionId);
				if (execution == null) {
					execution = new Execution(executionId, ExecutionSource.PACKAGING);
					execution.setDefaultActive(true);
					execution.setInherited(true);
					execution.addRestriction(new PackagingProcedureRestriction(packagingProcedure));
					executions.put(execution.getId(), execution);
				}
				Lifecycle lifecycle = execution.getLifecycle(lifecycleId);
				if (lifecycle == null) {
					lifecycle = new Lifecycle(lifecycleId);
					execution.putLifecycle(lifecycle);
				}
				Phase phase = lifecycle.getPhase(phaseMappingEntry.getKey());
				if (phase == null) {
					phase = new Phase(phaseMappingEntry.getKey());
					lifecycle.putPhase(phase);
				}

				phase.addGoal(goal.addModes("default", "dev"));
			}
		}
	}

}
