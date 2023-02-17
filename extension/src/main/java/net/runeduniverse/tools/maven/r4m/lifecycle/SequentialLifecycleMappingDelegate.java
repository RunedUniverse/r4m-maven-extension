package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecycleMappingDelegate;
import org.apache.maven.lifecycle.internal.builder.BuilderCommon;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.MojoExecution.Source;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelector;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.api.pem.ForkMappingDelegate;
import net.runeduniverse.tools.maven.r4m.api.pem.view.ExecutionView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.GoalView;

/**
 * Lifecycle mapping delegate component interface. Calculates project build
 * execution plan given {@link Lifecycle} and lifecycle phase. Standard
 * lifecycles use plugin execution {@code <phase>} or mojo default lifecycle
 * phase to calculate the execution plan, but custom lifecycles can use
 * alternative mapping strategies.
 */
@Component(role = LifecycleMappingDelegate.class, hint = SequentialLifecycleMappingDelegate.HINT)
public class SequentialLifecycleMappingDelegate implements LifecycleMappingDelegate {
	public static final String HINT = "dev";

	@Requirement
	private Logger log;
	@Requirement(role = Lifecycle.class)
	protected Map<String, Lifecycle> lifecycles;
	@Requirement
	private ExecutionArchiveSelector selector;
	@Requirement
	private ForkMappingDelegate forkMappingDelegate;

	public Map<String, List<MojoExecution>> calculateLifecycleMappings(MavenSession session, MavenProject project,
			Lifecycle devLifecycle, String devLifecyclePhase) throws PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException {

		// trim "dev-"
		String lifecyclePhase = devLifecyclePhase.substring(4);

		for (Lifecycle lifecycle : this.lifecycles.values())
			if (lifecycle.getPhases()
					.contains(lifecyclePhase)) {
				devLifecycle = lifecycle;
				break;
			}

		ExecutionArchiveSelectorConfig cnf = this.selector.createConfig();

		cnf.selectActiveProject(project);
		cnf.selectModes("dev");
		cnf.selectPackagingProcedure(project.getPackaging());

		ExecutionArchiveSelection selection = selector.compileSelection(cnf);

		/*
		 * Initialize mapping from lifecycle phase to bound mojos.
		 */

		Map<String, Map<Integer, List<MojoExecution>>> mappings = new LinkedHashMap<>();

		for (String phase : devLifecycle.getPhases()) {
			Map<Integer, List<MojoExecution>> phaseBindings = new TreeMap<>();

			mappings.put(phase, phaseBindings);

			if (phase.equals(lifecyclePhase)) {
				break;
			}
		}

		// select all lifecycle phases and exclude based on seeded mappings
		for (Entry<String, Map<ExecutionView, List<GoalView>>> phaseEntry : selection
				.selectPhases(devLifecycle.getPhases())
				.entrySet()) {

			for (Entry<ExecutionView, List<GoalView>> execEntry : phaseEntry.getValue()
					.entrySet()) {
				for (GoalView goal : execEntry.getValue()) {
					Map<Integer, List<MojoExecution>> phaseBindings = mappings.get(phaseEntry.getKey());
					if (phaseBindings != null) {
						MojoExecution mojoExecution = new MojoExecution(goal.getDescriptor(), execEntry.getKey()
								.getId(), Source.LIFECYCLE);
						mojoExecution.setLifecyclePhase(phaseEntry.getKey());
						if (goal.hasValidFork())
							mojoExecution.setForkedExecutions(BuilderCommon.getKey(project), this.forkMappingDelegate
									.calculateForkMappings(session, project, cnf, goal.getFork()));
						addMojoExecution(phaseBindings, mojoExecution, 0);
					}
				}
			}
		}

		Map<String, List<MojoExecution>> lifecycleMappings = new LinkedHashMap<>();

		for (Map.Entry<String, Map<Integer, List<MojoExecution>>> entry : mappings.entrySet()) {
			List<MojoExecution> mojoExecutions = new ArrayList<>();

			for (List<MojoExecution> executions : entry.getValue()
					.values()) {
				mojoExecutions.addAll(executions);
			}

			lifecycleMappings.put(entry.getKey(), mojoExecutions);
		}

		return lifecycleMappings;

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
