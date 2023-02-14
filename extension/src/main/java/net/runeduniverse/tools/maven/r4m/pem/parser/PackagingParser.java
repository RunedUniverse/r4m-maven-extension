package net.runeduniverse.tools.maven.r4m.pem.parser;

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

import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelPackagingParser;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

@Component(role = ProjectExecutionModelPackagingParser.class, hint = PackagingParser.HINT)
public class PackagingParser implements ProjectExecutionModelPackagingParser {

	public static final String HINT = "default";

	@Requirement
	private Logger log;

	@Requirement
	private Map<String, LifecycleMapping> mappings;

	@Override
	public ProjectExecutionModel parse() {
		this.log.debug("Scanning PackagingProcedures");
		Set<Execution> effExecutions = new LinkedHashSet<>();

		for (Entry<String, LifecycleMapping> lifecycleMappingEntry : mappings.entrySet()) {
			Map<String, Execution> executions = new LinkedHashMap<>();
			this.log.debug("PackagingProcedure: " + lifecycleMappingEntry.getKey());

			for (org.apache.maven.lifecycle.mapping.Lifecycle lifecycleMapping : lifecycleMappingEntry.getValue()
					.getLifecycles()
					.values())
				for (Entry<String, LifecyclePhase> phaseMappingEntry : lifecycleMapping.getLifecyclePhases()
						.entrySet()) {

					String executionId = String.join("-", Properties.DEFAULT_PACKAGING_PROCEDURE_EXECUTION_PREFIX,
							phaseMappingEntry.getKey());

					for (LifecycleMojo mojoMapping : phaseMappingEntry.getValue()
							.getMojos()) {
						Execution execution = executions.get(executionId);
						if (execution == null) {
							execution = new Execution(executionId, ExecutionSource.PACKAGING);
							execution.setDefaultActive(true);
							execution.addPackagingProcedure(lifecycleMappingEntry.getKey());
							executions.put(execution.getId(), execution);
						}
						Lifecycle lifecycle = execution.getLifecycle(lifecycleMapping.getId());
						if (lifecycle == null) {
							lifecycle = new Lifecycle(lifecycleMapping.getId());
							execution.addLifecycle(lifecycle);
						}
						Phase phase = lifecycle.getPhase(phaseMappingEntry.getKey());
						if (phase == null) {
							phase = new Phase(phaseMappingEntry.getKey());
							lifecycle.putPhase(phase);
						}

						phase.addGoal(new Goal(mojoMapping.getGoal()).addModes("default", "dev"));
					}
				}
			effExecutions.addAll(executions.values());
		}

		ProjectExecutionModel model = new ProjectExecutionModel(HINT);
		model.addExecutions(effExecutions);
		return model;
	}

}
