package net.runeduniverse.tools.maven.r4m.lifecycles.main;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecycleMappingDelegate;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelector;

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

	public Map<String, List<MojoExecution>> calculateLifecycleMappings(MavenSession session, MavenProject project,
			Lifecycle lifecycle, String lifecyclePhase) throws PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException {

		ExecutionArchiveSelector selector = archive.newSelection();

		selector.selectActiveProject(project);
		selector.selectModes("default");
		selector.selectPackagingProcedure(project.getPackaging());

		ExecutionArchiveSelection selection = selector.compile();

		this.log.warn("Active Execution Selection:");
		this.log.warn(selection.toRecord().toString());

		System.out.println("OVERRIDE Sequential >> Lifecycle: " + lifecycle + " Phase: " + lifecyclePhase);

		/*
		 * Initialize mapping from lifecycle phase to bound mojos.
		 */

		Map<String, Map<Integer, List<MojoExecution>>> mappings = new LinkedHashMap<>();

		mappings.put(lifecyclePhase, new TreeMap<>());

		/*
		 * Grab plugin executions that are bound to the selected lifecycle phases from
		 * project. The effective model of the project already contains the plugin
		 * executions induced by the project's packaging type. Remember, all phases of
		 * interest and only those are in the lifecycle mapping, if a phase has no value
		 * in the map, we are not interested in any of the executions bound to it.
		 */

		for (Plugin plugin : project.getBuild().getPlugins()) {
			// System.out.println(plugin);
			for (PluginExecution execution : plugin.getExecutions()) {
				// System.out.println("Execution: " + execution.getId());
				// if the phase is specified then I don't have to go fetch the plugin yet and
				// pull it down
				// to examine the phase it is associated to.
				// System.out.println("Phase: "+execution.getPhase());

				if (execution.getPhase() != null) {
					Map<Integer, List<MojoExecution>> phaseBindings = mappings.get(execution.getPhase());
					if (phaseBindings != null) {
						for (String goal : execution.getGoals()) {
							// System.out.println(" Phase: "+execution.getPhase()+" Goal: "+goal);

							MojoExecution mojoExecution = new MojoExecution(plugin, goal, execution.getId());
							mojoExecution.setLifecyclePhase(execution.getPhase());
							addMojoExecution(phaseBindings, mojoExecution, execution.getPriority());
						}
					}
				}
				// if not then i need to grab the mojo descriptor and look at the phase that is
				// specified
				else {
					for (String goal : execution.getGoals()) {
						MojoDescriptor mojoDescriptor = pluginManager.getMojoDescriptor(plugin, goal,
								project.getRemotePluginRepositories(), session.getRepositorySession());
						// System.out.println(" Phase: " + mojoDescriptor.getPhase() + "\n execute
						// Phase: "
						// + mojoDescriptor.getExecutePhase() + "\n Goal: " + mojoDescriptor.getGoal()
						// + "\n execute Goal:" + mojoDescriptor.getExecuteGoal());
						Map<Integer, List<MojoExecution>> phaseBindings = mappings.get(mojoDescriptor.getPhase());
						if (phaseBindings != null) {
							MojoExecution mojoExecution = new MojoExecution(mojoDescriptor, execution.getId());
							mojoExecution.setLifecyclePhase(mojoDescriptor.getPhase());
							addMojoExecution(phaseBindings, mojoExecution, execution.getPriority());
						}
					}
				}
			}
		}

		Map<String, List<MojoExecution>> lifecycleMappings = new LinkedHashMap<>();

		for (Map.Entry<String, Map<Integer, List<MojoExecution>>> entry : mappings.entrySet()) {
			List<MojoExecution> mojoExecutions = new ArrayList<>();

			for (List<MojoExecution> executions : entry.getValue().values()) {
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
