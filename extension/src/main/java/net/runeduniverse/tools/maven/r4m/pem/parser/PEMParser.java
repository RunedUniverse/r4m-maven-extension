package net.runeduniverse.tools.maven.r4m.pem.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionTriggerParser;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelParser;
import net.runeduniverse.tools.maven.r4m.api.pem.model.*;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ProjectExecutionModelParser.class, hint = "default")
public class PEMParser implements ProjectExecutionModelParser {

	@Requirement
	private Map<String, ExecutionTriggerParser> execTriggerParser;

	@Override
	public ProjectExecutionModel parseModel(InputStream input) throws IOException, XmlPullParserException {
		Reader reader = ReaderFactory.newXmlReader(input);

		ProjectExecutionModel pem = new ProjectExecutionModel();
		PlexusConfiguration cnf = new XmlPlexusConfiguration(Xpp3DomBuilder.build(reader));

		parseModelVersion(pem, cnf.getChild("modelVersion", false));
		parseExecutions(pem, cnf.getChild("executions", false));

		return pem;
	}

	protected boolean parseModelVersion(final ProjectExecutionModel model, final PlexusConfiguration versionNode) {
		if (versionNode == null)
			return false;
		String value = versionNode.getValue();
		if (isBlank(value))
			return false;
		model.setVersion(value);
		return true;
	}

	protected boolean parseExecutions(final ProjectExecutionModel model, final PlexusConfiguration nodeList) {
		if (nodeList == null || nodeList.getChildCount() == 0)
			return false;

		PlexusConfiguration execNodes[] = nodeList.getChildren("execution");
		if (execNodes.length > 0) {
			List<Execution> executions = new LinkedList<>();
			for (PlexusConfiguration execNode : execNodes) {
				parseExecution(executions, execNode);
			}
			model.addExecutions(executions);
		}
		return true;
	}

	protected boolean parseExecution(final Collection<Execution> list, final PlexusConfiguration execNode) {
		String id = execNode.getAttribute("id");
		ExecutionSource source = ExecutionSource.create(execNode.getAttribute("source"));
		if (isBlank(id) || source == null)
			return false;

		Execution execution = new Execution(id, source);
		list.add(execution);

		parseTriggers(execution, execNode.getChild("triggers", false));
		parseLifecycles(execution, execNode.getChild("lifecycles", false));

		return true;
	}

	protected boolean parseTriggers(final Execution exec, final PlexusConfiguration nodeList) {
		if (nodeList == null || nodeList.getChildCount() == 0)
			return false;

		PlexusConfiguration triggerNodes[] = nodeList.getChildren();
		if (triggerNodes.length > 0) {
			for (PlexusConfiguration triggerNode : triggerNodes) {
				String name = triggerNode.getName();
				switch (name) {
				case "on-call":
					// DO NOTING
					// every execution is active when called
					// except when flagged with <never/>
					break;
				case "always":
					exec.setAlwaysActive(true);
					break;
				case "default":
					exec.setDefaultActive(true);
					break;
				case "never":
					exec.setNeverActive(true);
					break;
				case "packaging-procedure":
					String value = triggerNode.getValue();
					if (!isBlank(value))
						exec.addPackagingProcedure(value);
					break;

				default:
					ExecutionTriggerParser parser = this.execTriggerParser.get(name);
					if (parser == null)
						break;
					Trigger trigger = parser.parse(triggerNode);
					if (trigger == null)
						break;
					exec.addTrigger(trigger);
					break;
				}
			}
		}
		return true;
	}

	protected boolean parseLifecycles(final Execution exec, final PlexusConfiguration nodeList) {
		if (nodeList == null || nodeList.getChildCount() == 0)
			return false;

		PlexusConfiguration lifecycleNodes[] = nodeList.getChildren("lifecycle");
		if (lifecycleNodes.length > 0) {
			List<Lifecycle> lifecycles = new LinkedList<>();
			for (PlexusConfiguration lifecycleNode : lifecycleNodes) {
				parseLifecycle(lifecycles, lifecycleNode);
			}
			exec.addLifecycles(lifecycles);
		}
		return true;
	}

	protected boolean parseLifecycle(final Collection<Lifecycle> list, final PlexusConfiguration lifecycleNode) {
		String id = lifecycleNode.getAttribute("id");
		if (isBlank(id))
			return false;

		Lifecycle lifecycle = new Lifecycle(id);
		list.add(lifecycle);

		parsePhases(lifecycle, lifecycleNode.getChild("phases", false));

		return true;
	}

	protected boolean parsePhases(final Lifecycle lifecycle, final PlexusConfiguration nodeList) {
		if (nodeList == null || nodeList.getChildCount() == 0)
			return false;

		PlexusConfiguration phaseNodes[] = nodeList.getChildren("phase");
		if (phaseNodes.length > 0) {
			List<Phase> phases = new LinkedList<>();
			for (PlexusConfiguration phaseNode : phaseNodes) {
				parsePhase(phases, phaseNode);
			}
			lifecycle.addPhases(phases);
		}
		return true;
	}

	protected boolean parsePhase(final Collection<Phase> list, final PlexusConfiguration phaseNode) {
		String id = phaseNode.getAttribute("id");
		if (isBlank(id))
			return false;

		Phase phase = new Phase(id);
		list.add(phase);

		parseGoals(phase, phaseNode.getChild("goals", false));

		return true;
	}

	protected boolean parseGoals(final Phase phase, final PlexusConfiguration nodeList) {
		if (nodeList == null || nodeList.getChildCount() == 0)
			return false;

		PlexusConfiguration goalNodes[] = nodeList.getChildren("goal");
		if (goalNodes.length > 0) {
			List<Goal> goals = new LinkedList<>();
			for (PlexusConfiguration goalNode : goalNodes) {
				parseGoal(goals, goalNode);
			}
			phase.addGoals(goals);
		}
		return true;
	}

	protected boolean parseGoal(final Collection<Goal> list, final PlexusConfiguration goalNode) {
		String id = goalNode.getAttribute("id");
		if (isBlank(id))
			return false;

		String groupId = goalNode.getChild("groupId")
				.getValue();
		String artifactId = goalNode.getChild("artifactId")
				.getValue();
		String goalId = goalNode.getAttribute("id");
		if (isBlank(groupId) || isBlank(artifactId) || isBlank(goalId))
			return false;

		Goal goal = new Goal(groupId, artifactId, goalId);

		final PlexusConfiguration modesNode = goalNode.getChild("modes", true);
		Set<String> modes = new LinkedHashSet<>(0);
		for (PlexusConfiguration modeNode : modesNode.getChildren()) {
			String name = modeNode.getName();
			if (!isBlank(name))
				modes.add(name);
		}
		// no modes selected!
		if (modes.isEmpty())
			return false;
		goal.addModes(modes);

		parseFork(goal, goalNode.getChild("fork", false));

		list.add(goal);

		return true;
	}

	protected boolean parseFork(final Goal goal, final PlexusConfiguration forkNode) {
		Fork fork = new Fork();

		final PlexusConfiguration modeNode = forkNode.getChild("mode", false);
		if (modeNode != null)
			fork.setMode(modeNode.getValue());

		Set<String> executions = new LinkedHashSet<>(0);
		parseTargetExecutions(executions, forkNode.getChild("executions", false));
		fork.addExecutions(executions);

		parseTargetLifecycle(fork, forkNode.getChild("lifecycle", false));

		List<TargetPhase> phases = new LinkedList<>();
		parseTargetPhases(phases, forkNode.getChild("phases", false));
		fork.setPhases(phases);

		Set<TargetPhase> excludedPhases = new LinkedHashSet<>(0);
		parseTargetPhases(excludedPhases, forkNode.getChild("excludedPhases", false));
		fork.addExcludedPhases(excludedPhases);

		goal.setFork(fork);
		return true;
	}

	protected boolean parseTargetLifecycle(final Fork fork, final PlexusConfiguration lifecycleNode) {
		String id = lifecycleNode.getAttribute("id");
		if (isBlank(id))
			return false;

		TargetLifecycle lifecycle = new TargetLifecycle(id);

		final PlexusConfiguration startNode = lifecycleNode.getChild("startPhase", false);
		if (startNode != null)
			lifecycle.setStartPhase(startNode.getValue());

		final PlexusConfiguration stopNode = lifecycleNode.getChild("stopPhase", false);
		if (startNode != null)
			lifecycle.setStopPhase(stopNode.getValue());

		fork.setLifecycle(lifecycle);
		return true;
	}

	protected boolean parseTargetPhases(final Collection<TargetPhase> targetPhases,
			final PlexusConfiguration phasesNode) {
		if (phasesNode == null || phasesNode.getChildCount() == 0)
			return false;

		PlexusConfiguration phaseNodes[] = phasesNode.getChildren("phase");
		if (phaseNodes.length > 0) {
			for (PlexusConfiguration phaseNode : phaseNodes)
				parseTargetPhase(targetPhases, phaseNode);
		}
		return true;
	}

	protected boolean parseTargetPhase(final Collection<TargetPhase> list, final PlexusConfiguration phaseNode) {
		String id = phaseNode.getAttribute("id");
		if (isBlank(id))
			return false;

		TargetPhase phase = new TargetPhase(id);
		list.add(phase);

		Set<String> executions = new LinkedHashSet<>(0);
		parseTargetExecutions(executions, phaseNode.getChild("executions", false));
		phase.addExecutions(executions);

		return true;
	}

	protected boolean parseTargetExecutions(final Collection<String> executions,
			final PlexusConfiguration executionsNode) {
		if (executionsNode == null || executionsNode.getChildCount() == 0)
			return false;

		PlexusConfiguration execNodes[] = executionsNode.getChildren("execution");
		if (execNodes.length > 0) {
			for (PlexusConfiguration execNode : execNodes)
				parseTargetExecution(executions, execNode);
		}
		return true;
	}

	protected boolean parseTargetExecution(final Collection<String> list, final PlexusConfiguration execNode) {
		String value = execNode.getValue();
		if (isBlank(value))
			return false;

		list.add(value);
		return true;
	}
}
