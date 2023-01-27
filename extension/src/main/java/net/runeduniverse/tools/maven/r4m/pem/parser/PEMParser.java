package net.runeduniverse.tools.maven.r4m.pem.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelParser;
import net.runeduniverse.tools.maven.r4m.api.pem.model.*;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ProjectExecutionModelParser.class, hint = "default")
public class PEMParser implements ProjectExecutionModelParser {

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

				// TODO parse trigger
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

		parseGoals(phase, phaseNode);

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

		Goal goal = new Goal();
		// TODO parse ???

		list.add(goal);

		return true;
	}

}
