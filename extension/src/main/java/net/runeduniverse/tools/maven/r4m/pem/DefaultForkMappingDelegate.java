package net.runeduniverse.tools.maven.r4m.pem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecution.Source;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelector;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.api.pem.ForkMappingDelegate;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.api.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.api.pem.model.TargetPhase;
import net.runeduniverse.tools.maven.r4m.api.pem.view.ExecutionView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.GoalView;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ForkMappingDelegate.class, hint = DefaultForkMappingDelegate.HINT)
public class DefaultForkMappingDelegate implements ForkMappingDelegate {
	public static final String HINT = "default";

	@Requirement
	private Logger log;
	@Requirement(role = Lifecycle.class)
	protected Map<String, Lifecycle> lifecycles;
	@Requirement
	private ExecutionArchiveSelector selector;

	protected List<String> calculateExecutingPhases(final Map<String, Set<String>> executionsPerPhase,
			final Fork fork) {
		// seed executing phases which might be defined
		List<String> phases = new LinkedList<>();
		if (fork.getPhases() != null)
			for (TargetPhase phase : fork.getPhases()) {
				phases.add(phase.getId());
				executionsPerPhase.put(phase.getId(), phase.getExecutions());
			}
		// seed phases if not set & existent from lifecycle
		TargetLifecycle lifecycle = fork.getLifecycle();
		if (lifecycle != null && phases.isEmpty()) {
			boolean includePhase = isBlank(lifecycle.getStartPhase());
			for (String phaseId : this.lifecycles.get(lifecycle.getId())
					.getPhases()) {
				if (includePhase) {
					phases.add(phaseId);
					executionsPerPhase.put(phaseId, new LinkedHashSet<>(0));
				}
				if (phaseId != null && phaseId.equals(lifecycle.getStopPhase()))
					break;
			}
		}
		return phases;
	}

	protected Map<String, Set<String>> calculateExcludedPhases(final Fork fork) {
		// seed excluded phases which might be defined
		Map<String, Set<String>> excludePhases = new LinkedHashMap<>(0);
		for (TargetPhase phase : fork.getExcludedPhases())
			excludePhases.put(phase.getId(), phase.getExecutions());
		return excludePhases;
	}

	protected List<MojoExecution> calculateForkMappings(final Fork fork, final ExecutionArchiveSelection selection) {

		Map<String, Set<String>> executionsPerPhase = new LinkedHashMap<>();
		List<String> orderedPhases = calculateExecutingPhases(executionsPerPhase, fork);
		Map<String, Set<String>> excludePhases = calculateExcludedPhases(fork);

		// Initialize mapping from lifecycle phase to bound mojos.
		Map<String, Map<Integer, List<MojoExecution>>> mappings = new LinkedHashMap<>();
		for (String phase : orderedPhases) {
			mappings.put(phase, new TreeMap<>());
			Set<String> requiredExecutions = executionsPerPhase.get(phase);
			for (Entry<ExecutionView, List<GoalView>> entry : selection.selectPhase(phase)
					.entrySet()) {
				String executionId = entry.getKey()
						.getId();
				// if executions are explicitly defined, only run those
				if (!requiredExecutions.isEmpty() && !requiredExecutions.contains(executionId))
					continue;
				if (excludePhases.containsKey(executionId)) {
					Set<String> excludedExecutions = excludePhases.get(executionId);
					if (excludedExecutions.isEmpty())
						continue;
					else if (excludedExecutions.contains(executionId))
						continue;
				}

				for (GoalView goal : entry.getValue()) {
					Map<Integer, List<MojoExecution>> phaseBindings = mappings.get(phase);
					if (phaseBindings != null) {
						MojoExecution mojoExecution = new MojoExecution(goal.getDescriptor(), executionId,
								Source.LIFECYCLE);
						mojoExecution.setLifecyclePhase(phase);
						addMojoExecution(phaseBindings, mojoExecution, 0);
					}
				}
			}
		}

		// reduce and order MojoExecution's
		List<MojoExecution> lifecycleMappings = new LinkedList<>();

		for (String phase : orderedPhases)
			for (List<MojoExecution> entryExecutions : mappings.get(phase)
					.values())
				lifecycleMappings.addAll(entryExecutions);

		return lifecycleMappings;
	}

	@Override
	public List<MojoExecution> calculateForkMappings(final MavenSession mvnSession, final MavenProject mvnProject,
			final ExecutionArchiveSelectorConfig baseCnf, final Fork fork) {

		ExecutionArchiveSelectorConfig cnf = this.selector.createConfig();
		// select mvnProject
		cnf.selectActiveProject(mvnProject);
		// select packaging-procedure
		cnf.selectPackagingProcedure(baseCnf.getPackagingProcedure());
		// select modes
		if (isBlank(fork.getMode()))
			cnf.selectModes(baseCnf.getModes());
		else
			cnf.selectModes(fork.getMode());
		// select executions
		cnf.selectActiveExecutions(fork.getExecutions());

		return calculateForkMappings(fork, this.selector.compileSelection(cnf));
	}

	private void addMojoExecution(Map<Integer, List<MojoExecution>> phaseBindings, MojoExecution mojoExecution,
			int priority) {
		List<MojoExecution> mojoExecutions = phaseBindings.get(priority);

		if (mojoExecutions == null) {
			mojoExecutions = new ArrayList<>();
			phaseBindings.put(priority, mojoExecutions);
		}

		mojoExecutions.add(mojoExecution);
	}
}
