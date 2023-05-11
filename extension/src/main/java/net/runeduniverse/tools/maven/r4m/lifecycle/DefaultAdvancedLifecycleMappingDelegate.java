package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.internal.builder.BuilderCommon;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecution.Source;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.maven.r4m.api.lifecycle.AdvancedLifecycleMappingDelegate;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;
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
@Component(role = AdvancedLifecycleMappingDelegate.class, hint = DefaultAdvancedLifecycleMappingDelegate.HINT)
public class DefaultAdvancedLifecycleMappingDelegate implements AdvancedLifecycleMappingDelegate {

	public static final String HINT = "default";

	@Requirement
	private Logger log;
	@Requirement
	private ForkMappingDelegate forkMappingDelegate;

	public Map<String, List<MojoExecution>> calculateLifecycleMappings(MavenSession session, MavenProject project,
			Lifecycle lifecycle, String lifecyclePhase, ExecutionArchiveSelection selection)
			throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
			MojoNotFoundException, InvalidPluginDescriptorException {
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
					MojoExecutionAdapter mojoExecution = new MojoExecutionAdapter(goal.getDescriptor(), entry.getKey()
							.getId(), Source.LIFECYCLE, selection.getSelectorConfig());
					mojoExecution.setLifecyclePhase(lifecyclePhase);
					mojoExecution.setFork(goal.getFork());
					if (goal.hasValidFork())
						try {
							mojoExecution.setForkedExecutions(BuilderCommon.getKey(project),
									this.forkMappingDelegate.calculateForkMappings(mojoExecution, session, project));
						} catch (MojoNotFoundException | NoPluginFoundForPrefixException
								| PluginVersionResolutionException e) {
							this.log.error("Unable to fork!", e);
						}
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
