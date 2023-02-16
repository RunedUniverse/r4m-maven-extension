package net.runeduniverse.tools.maven.r4m.lifecycles.main;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecycleMappingDelegate;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecution.Source;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelector;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.api.pem.view.ExecutionView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.GoalView;

/**
 * Lifecycle mapping delegate component interface. Calculates project build
 * execution plan given {@link Lifecycle} and lifecycle phase. Standard
 * lifecycles use plugin execution {@code <phase>} or mojo default lifecycle
 * phase to calculate the execution plan, but custom lifecycles can use
 * alternative mapping strategies.
 */
@Component(role = LifecycleMappingDelegate.class, hint = MainLifecycleMappingDelegate.HINT)
public class MainLifecycleMappingDelegate implements LifecycleMappingDelegate {
	public static final String HINT = "default";

	@Requirement
	Logger log;
	@Requirement
	private BuildPluginManager pluginManager;
	@Requirement
	private ExecutionArchive archive;
	@Requirement
	ExecutionArchiveSelector selector;

	public Map<String, List<MojoExecution>> calculateLifecycleMappings(MavenSession session, MavenProject project,
			Lifecycle lifecycle, String lifecyclePhase) throws PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException {

		ExecutionArchiveSelectorConfig cnf = this.selector.createConfig();

		cnf.selectActiveProject(project);
		cnf.selectModes("default");
		cnf.selectPackagingProcedure(project.getPackaging());

		ExecutionArchiveSelection selection = selector.compileSelection(cnf);

		this.log.warn(cnf.toRecord()
				.toString());
		this.log.warn("Active Execution Selection:");
		this.log.warn(selection.toRecord()
				.toString());

		System.out.println("OVERRIDE Sequential >> Lifecycle: " + lifecycle + " Phase: " + lifecyclePhase);

		/*
		 * Initialize mapping from lifecycle phase to bound mojos.
		 */

		Map<String, Map<Integer, List<MojoExecution>>> mappings = new LinkedHashMap<>();

		mappings.put(lifecyclePhase, new TreeMap<>());

		for (Entry<ExecutionView, List<GoalView>> entry : selection.selectPhase(lifecyclePhase)
				.entrySet()) {
			for (GoalView goal : entry.getValue()) {
				Map<Integer, List<MojoExecution>> phaseBindings = mappings.get(lifecyclePhase);
				if (phaseBindings != null) {
					MojoExecution mojoExecution = new MojoExecution(goal.getDescriptor(), entry.getKey()
							.getId(), Source.LIFECYCLE);
					mojoExecution.setLifecyclePhase(lifecyclePhase);
					// TODO implement forking !!!
					addMojoExecution(phaseBindings, mojoExecution, 0);
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

	private void addMojoExecution(final Map<Integer, List<MojoExecution>> phaseBindings,
			final MojoExecution mojoExecution, final int priority) {
		List<MojoExecution> mojoExecutions = phaseBindings.get(priority);

		if (mojoExecutions == null) {
			mojoExecutions = new ArrayList<>();
			phaseBindings.put(priority, mojoExecutions);
		}

		mojoExecutions.add(mojoExecution);
	}

}
