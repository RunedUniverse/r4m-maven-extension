package net.runeduniverse.tools.maven.r4m.pem.writer;

import java.io.OutputStream;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionRestrictionWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionTriggerWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.api.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.api.pem.model.TargetPhase;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ProjectExecutionModelWriter.class, hint = "default")
public class XmlWriter implements ProjectExecutionModelWriter {

	@Requirement(role = ExecutionRestrictionWriter.class)
	private Map<String, ExecutionRestrictionWriter> restrictionWriter;
	@Requirement(role = ExecutionTriggerWriter.class)
	private Map<String, ExecutionTriggerWriter> triggerWriter;

	@Override
	public void writeModel(OutputStream stream, ProjectExecutionModel pem) {
		// TODO Auto-generated method stub

	}

	@Override
	public PlexusConfiguration convert(final ProjectExecutionModel pem) {
		PlexusConfiguration node = new XmlPlexusConfiguration("project-execution-model");
		node.setAttribute("xmlns", "https://api.runeduniverse.net/runes4tools/r4m-pem");
		node.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		node.setAttribute("xsi:schemaLocation",
				"https://api.runeduniverse.net/runes4tools/r4m-pem https://api.runeduniverse.net/runes4tools/r4m-pem-v"
						+ Properties.PROJECT_EXECUTION_MODEL_VERSION.replace('.', '_') + ".xsd");
		node.addChild("modelVersion", Properties.PROJECT_EXECUTION_MODEL_VERSION);

		PlexusConfiguration executionsNode = node.getChild("executions", true);
		for (Execution exec : pem.getExecutions())
			executionsNode.addChild(convert(exec));

		return node;
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
			for (ExecutionRestriction restriction : execution.getRestrictions()) {
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
		for (ExecutionTrigger trigger : execution.getTrigger()) {
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
			for (String mode : goal.getModes())
				modeNodes.addChild(mode, null);
		}

		if (goal.hasFork())
			node.addChild(convert(goal.getFork()));

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

		if (fork.getMode() != null)
			node.addChild("mode", fork.getMode());

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
