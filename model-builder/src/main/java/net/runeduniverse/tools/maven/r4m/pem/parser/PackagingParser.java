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

import net.runeduniverse.tools.maven.r4m.api.MavenProperties;
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
		Set<Execution> effExecutions = new LinkedHashSet<>();

		for (Entry<String, LifecycleMapping> lifecycleMappingEntry : this.mappings.entrySet()) {
			Set<String> allLifecycleIds = new HashSet<>(this.defaultLifecycles.keySet());
			Map<String, Execution> executions = new LinkedHashMap<>();
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

		ProjectExecutionModel model = new ProjectExecutionModel(PackagingParser.class, PackagingParser.HINT);
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
