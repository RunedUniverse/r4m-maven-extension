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
package net.runeduniverse.tools.maven.r4m.pem.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionRestrictionWriter;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionTriggerWriter;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelWriter;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelProperties;
import net.runeduniverse.tools.maven.r4m.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetPhase;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ProjectExecutionModelWriter.class, hint = "xml", instantiationStrategy = "singleton")
public class XmlWriter implements ProjectExecutionModelWriter {

	@Requirement(role = ExecutionRestrictionWriter.class)
	private Map<String, ExecutionRestrictionWriter> restrictionWriter;
	@Requirement(role = ExecutionTriggerWriter.class)
	private Map<String, ExecutionTriggerWriter> triggerWriter;

	@Override
	public void writeModel(OutputStream stream, ProjectExecutionModel pem) throws IOException {
		stream.write(convert(pem).toString()
				.getBytes());
	}

	@Override
	public void writeModel(OutputStream stream, ProjectExecutionModel pem, Charset charset) throws IOException {
		stream.write(convert(pem).toString()
				.getBytes(charset));
	}

	@Override
	public void writeModel(OutputStream stream, ProjectExecutionModel pem, String charsetName) throws IOException {
		stream.write(convert(pem).toString()
				.getBytes(charsetName));
	}

	@Override
	public PlexusConfiguration convert(final ProjectExecutionModel pem) {
		final String version = getVersion(pem);
		final PlexusConfiguration node = new XmlPlexusConfiguration("project-execution-model");
		node.setAttribute("xmlns", "https://api.runeduniverse.net/runes4tools/r4m-pem");
		node.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		node.setAttribute("xsi:schemaLocation",
				"https://api.runeduniverse.net/runes4tools/r4m-pem https://api.runeduniverse.net/runes4tools/r4m-pem-v"
						+ version.replace('.', '_') + ".xsd");
		node.addChild("modelVersion", version);

		final PlexusConfiguration executionsNode = node.getChild("executions", true);
		for (Execution exec : pem.getExecutions())
			executionsNode.addChild(convert(exec));

		return node;
	}

	protected String getVersion(final ProjectExecutionModel pem) {
		final String version = pem.getVersion();
		if (isBlank(version))
			return ModelProperties.MODEL_VERSION;
		return version;
	}

	@Override
	public PlexusConfiguration convert(final Execution execution) {
		PlexusConfiguration node = new XmlPlexusConfiguration("execution");
		node.setAttribute("source", execution.getSource()
				.key());
		node.setAttribute("id", execution.getId());

		if (!execution.isInherited())
			node.addChild("inherited", "false");

		if (!execution.getRestrictions()
				.isEmpty()) {
			PlexusConfiguration restrictionNodes = node.getChild("restrictions", true);
			for (ExecutionRestriction<?> restriction : execution.getRestrictions()) {
				ExecutionRestrictionWriter writer = this.restrictionWriter.get(restriction.getHint());
				if (writer != null)
					writer.append(restrictionNodes, restriction);
			}
		}

		PlexusConfiguration triggerNodes = new XmlPlexusConfiguration("triggers");
		if (execution.isAlwaysActive())
			triggerNodes.addChild("always", null);
		if (execution.isDefaultActive())
			triggerNodes.addChild("default", null);
		if (execution.isNeverActive())
			triggerNodes.addChild("never", null);
		for (ExecutionTrigger<?> trigger : execution.getTrigger()) {
			ExecutionTriggerWriter writer = this.triggerWriter.get(trigger.getHint());
			if (writer != null)
				writer.append(triggerNodes, trigger);
		}
		if (0 < triggerNodes.getChildCount())
			node.addChild(triggerNodes);

		PlexusConfiguration lifecycleNodes = node.getChild("lifecycles", true);
		for (Lifecycle lifecycle : execution.getLifecycles()
				.values())
			lifecycleNodes.addChild(convert(lifecycle));

		return node;
	}

	@Override
	public PlexusConfiguration convert(final Lifecycle lifecycle) {
		PlexusConfiguration node = new XmlPlexusConfiguration("lifecycle");
		node.setAttribute("id", lifecycle.getId());

		PlexusConfiguration phaseNodes = node.getChild("phases", true);
		for (Phase phase : lifecycle.getPhases()
				.values())
			phaseNodes.addChild(convert(phase));

		return node;
	}

	@Override
	public PlexusConfiguration convert(final Phase phase) {
		PlexusConfiguration node = new XmlPlexusConfiguration("phase");
		node.setAttribute("id", phase.getId());

		PlexusConfiguration goalNodes = node.getChild("goals", true);
		for (Goal goal : phase.getGoals())
			goalNodes.addChild(convert(goal));

		return node;
	}

	@Override
	public PlexusConfiguration convert(final Goal goal) {
		PlexusConfiguration node = new XmlPlexusConfiguration("goal");
		node.setAttribute("id", goal.getGoalId());
		node.addChild("groupId", goal.getGroupId());
		node.addChild("artifactId", goal.getArtifactId());

		if (!goal.getModes()
				.isEmpty()) {
			PlexusConfiguration modeNodes = node.getChild("modes", true);
			for (String mode : goal.getModes()) {
				if (isBlank(mode))
					continue;
				modeNodes.addChild(convertMode(mode));
			}
		}

		if (goal.getOptional() != null)
			node.setAttribute("optional", goal.getOptional()
					.toString());

		if (goal.hasFork())
			node.addChild(convert(goal.getFork()));

		return node;
	}

	protected PlexusConfiguration convertMode(final String id) {
		PlexusConfiguration node = new XmlPlexusConfiguration("mode");
		node.setAttribute("id", id);
		return node;
	}

	@Override
	public PlexusConfiguration convert(final Fork fork) {
		PlexusConfiguration node = new XmlPlexusConfiguration("fork");

		if (!fork.getExecutions()
				.isEmpty()) {
			PlexusConfiguration executionNodes = node.getChild("executions", true);
			for (String execution : fork.getExecutions())
				executionNodes.getChild("execution", true)
						.setAttribute("id", execution);
		}

		if (!isBlank(fork.getMode()))
			node.addChild(convertMode(fork.getMode()));

		if (fork.getPhases() != null && !fork.getPhases()
				.isEmpty()) {
			PlexusConfiguration phaseNodes = node.getChild("phases", true);
			for (TargetPhase phase : fork.getPhases())
				phaseNodes.addChild(convert(phase));
		}

		if (!fork.getExcludedPhases()
				.isEmpty()) {
			PlexusConfiguration excludedPhaseNodes = node.getChild("excludedPhases", true);
			for (TargetPhase phase : fork.getExcludedPhases())
				excludedPhaseNodes.addChild(convert(phase));
		}

		if (fork.getLifecycle() != null)
			node.addChild(convert(fork.getLifecycle()));

		return node;
	}

	@Override
	public PlexusConfiguration convert(final TargetLifecycle targetLifecycle) {
		PlexusConfiguration node = new XmlPlexusConfiguration("lifecycle");
		node.setAttribute("id", targetLifecycle.getId());

		if (!isBlank(targetLifecycle.getStartPhase()))
			node.addChild("startPhase", targetLifecycle.getStartPhase());
		if (!isBlank(targetLifecycle.getStopPhase()))
			node.addChild("stopPhase", targetLifecycle.getStopPhase());

		return node;
	}

	@Override
	public PlexusConfiguration convert(final TargetPhase targetPhase) {
		PlexusConfiguration node = new XmlPlexusConfiguration("phase");
		node.setAttribute("id", targetPhase.getId());

		if (!targetPhase.getExecutions()
				.isEmpty()) {
			PlexusConfiguration executionNodes = node.getChild("executions", true);
			for (String execution : targetPhase.getExecutions())
				executionNodes.getChild("execution", true)
						.setAttribute("id", execution);
		}

		return node;
	}

}
