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
package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
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

import net.runeduniverse.tools.maven.r4m.lifecycle.api.AdvancedLifecycleMappingDelegate;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.pem.view.api.ExecutionView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.GoalView;

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

	@Override
	public Map<String, List<MojoExecution>> calculateLifecycleMappings(final MavenSession session,
			final MavenProject project, final Lifecycle lifecycle, final String lifecyclePhase,
			final ExecutionArchiveSelection selection) throws PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException {
		/*
		 * Initialize mapping from lifecycle phase to bound mojos.
		 */

		final Map<String, Map<Integer, List<MojoExecution>>> mappings = new LinkedHashMap<>();

		mappings.put(lifecyclePhase, new TreeMap<>());

		for (Entry<ExecutionView, List<GoalView>> entry : selection.selectPhase(lifecyclePhase)
				.entrySet()) {
			for (GoalView goal : entry.getValue()) {
				final Map<Integer, List<MojoExecution>> phaseBindings = mappings.get(lifecyclePhase);
				if (phaseBindings != null) {
					final MojoExecutionAdapter mojoExecution = new MojoExecutionAdapter(goal.getDescriptor(),
							entry.getKey()
									.getId(),
							Source.LIFECYCLE, selection.getSelectorConfig());
					mojoExecution.setLifecyclePhase(lifecyclePhase);
					mojoExecution.setFork(goal.getFork());
					addMojoExecution(phaseBindings, mojoExecution, 0);
				}
			}
		}

		final Map<String, List<MojoExecution>> lifecycleMappings = new LinkedHashMap<>();

		for (Map.Entry<String, Map<Integer, List<MojoExecution>>> entry : mappings.entrySet()) {
			final List<MojoExecution> mojoExecutions = new ArrayList<>();

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
		final List<MojoExecution> mojoExecutions = phaseBindings.computeIfAbsent(priority, k -> new ArrayList<>());
		mojoExecutions.add(mojoExecution);
	}

}
