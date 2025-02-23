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

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelPluginParser;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelProperties;
import net.runeduniverse.tools.maven.r4m.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetPhase;

@Component(role = ProjectExecutionModelPluginParser.class, hint = ExecutionsPluginParser.HINT)
public class ExecutionsPluginParser implements ProjectExecutionModelPluginParser {

	public static final String HINT = "plugin-execution";
	public static final String ERR_MSG_PLUGIN_DESCRIPTOR = "Failed to acquire org.apache.maven.plugin.descriptor.PluginDescriptor!";

	@Requirement
	protected Logger log;
	@Requirement
	protected MavenPluginManager manager;
	@Requirement(role = org.apache.maven.lifecycle.Lifecycle.class)
	private Map<String, org.apache.maven.lifecycle.Lifecycle> mvnLifecycles;

	@Override
	public ProjectExecutionModel parse(final List<RemoteRepository> repositories, final RepositorySystemSession session,
			final Plugin mvnPlugin) throws Exception {

		PluginDescriptor mvnPluginDescriptor = null;

		try {
			mvnPluginDescriptor = this.manager.getPluginDescriptor(mvnPlugin, repositories, session);
		} catch (PluginResolutionException | PluginDescriptorParsingException | InvalidPluginDescriptorException e) {
			this.log.error(ERR_MSG_PLUGIN_DESCRIPTOR, e);
			return null;
		}

		final ProjectExecutionModel model = new ProjectExecutionModel();
		model.setParser(ExecutionsPluginParser.class, ExecutionsPluginParser.HINT);

		model.setVersion(ModelProperties.MODEL_VERSION);

		for (PluginExecution mvnExecution : mvnPlugin.getExecutions()) {
			Execution execution = new Execution(mvnExecution.getId(), ExecutionSource.OVERRIDE);
			execution.setInherited(false);
			execution.setDefaultActive(true);

			if (mvnExecution.getPhase() != null) {
				Lifecycle lifecycle = acquireLifecycle(execution, mvnExecution.getPhase());
				if (lifecycle == null)
					continue;
				Phase phase = lifecycle.getPhase(mvnExecution.getPhase());
				if (phase == null)
					phase = new Phase(mvnExecution.getPhase());
				for (String goalId : mvnExecution.getGoals()) {
					MojoDescriptor mvnMojoDescriptor = mvnPluginDescriptor.getMojo(goalId);
					if (mvnMojoDescriptor == null)
						continue;
					Goal goal = createGoal(mvnPlugin, mvnMojoDescriptor);
					if (goal != null)
						phase.addGoal(goal);
				}
				if (phase.getGoals()
						.isEmpty())
					continue;
				lifecycle.putPhase(phase);
				execution.putLifecycle(lifecycle);
			} else
				for (String goalId : mvnExecution.getGoals()) {
					MojoDescriptor mvnMojoDescriptor = mvnPluginDescriptor.getMojo(goalId);
					if (mvnMojoDescriptor == null)
						continue;
					String phaseId = mvnMojoDescriptor.getPhase();
					if (isBlank(phaseId))
						continue;
					Lifecycle lifecycle = acquireLifecycle(execution, phaseId);
					if (lifecycle == null)
						continue;
					Phase phase = lifecycle.getPhase(phaseId);
					if (phase == null)
						phase = new Phase(phaseId);
					Goal goal = createGoal(mvnPlugin, mvnMojoDescriptor);
					if (goal == null)
						continue;
					phase.addGoal(goal);
					lifecycle.putPhase(phase);
					execution.putLifecycle(lifecycle);
				}

			if (!execution.getLifecycles()
					.isEmpty())
				model.addExecution(execution);
		}
		return model;
	}

	private Goal createGoal(final Plugin mvnPlugin, final MojoDescriptor mvnMojoDescriptor) {
		Goal goal = new Goal(mvnPlugin.getGroupId(), mvnPlugin.getArtifactId(), mvnMojoDescriptor.getGoal())
				.addModes("default", "dev");
		Fork fork = new Fork();

		String executeGoal = mvnMojoDescriptor.getExecuteGoal();
		if (!isBlank(executeGoal)) {
			// TODO maybe add this as a feature?
			/*
			 * anyway the goal gets forked as long as no phases (or lifecycles) get defined
			 * through {@link MojoDescriptor}, PEM or other means
			 *
			 * (extension version: >1.1.0)
			 */
		}

		String executePhase = mvnMojoDescriptor.getExecutePhase();
		if (!isBlank(executePhase)) {
			List<TargetPhase> phases = new LinkedList<>();
			fork.setPhases(phases);
			phases.add(new TargetPhase(executePhase));
		}
		String executeLifecycle = mvnMojoDescriptor.getExecuteLifecycle();
		if (!isBlank(executeLifecycle))
			fork.setLifecycle(new TargetLifecycle(executeLifecycle));

		if (fork.isValid())
			goal.setFork(fork);
		return goal;
	}

	private Lifecycle acquireLifecycle(final Execution execution, final String phase) {
		String id = null;
		for (Entry<String, org.apache.maven.lifecycle.Lifecycle> entry : this.mvnLifecycles.entrySet())
			if (entry.getValue()
					.getPhases()
					.contains(phase)) {
				id = entry.getKey();
				break;
			}
		if (id == null)
			return null;
		Lifecycle lifecycle = execution.getLifecycle(id);
		if (lifecycle != null)
			return lifecycle;
		return new Lifecycle(id);
	}
}
