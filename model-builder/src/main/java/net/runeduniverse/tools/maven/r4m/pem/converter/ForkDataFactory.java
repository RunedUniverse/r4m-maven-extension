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
package net.runeduniverse.tools.maven.r4m.pem.converter;

import java.util.function.Consumer;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataFactory;
import net.runeduniverse.tools.maven.r4m.pem.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetPhase;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = DataFactory.class, hint = Fork.HINT)
public class ForkDataFactory extends ADataFactory {

	@Override
	public DataEntry createEntry(final PlexusConfiguration cnf) {
		if (!Fork.HINT.equals(cnf.getName()))
			return null;

		final Fork fork = new Fork();

		final PlexusConfiguration modeNode = cnf.getChild("mode", false);
		if (modeNode != null) {
			fork.setMode(getAttributeAsId(modeNode, "id"));
		}

		parseTargetExecutions(cnf.getChild("executions", false), fork::addExecution);

		parseTargetLifecycle(fork, cnf.getChild("lifecycle", false));

		parseTargetPhases(cnf.getChild("phases", false), fork::addPhase);

		parseTargetPhases(cnf.getChild("excludedPhases", false), fork::addExcludedPhase);

		return fork;
	}

	protected boolean parseTargetLifecycle(final Fork fork, final PlexusConfiguration lifecycleNode) {
		if (lifecycleNode == null)
			return false;

		final String id = getAttributeAsId(lifecycleNode, "id");
		if (isBlank(id))
			return false;

		final TargetLifecycle lifecycle = new TargetLifecycle(id);

		final PlexusConfiguration startNode = lifecycleNode.getChild("startPhase", false);
		if (startNode != null)
			lifecycle.setStartPhase(getValueAsId(startNode));

		final PlexusConfiguration stopNode = lifecycleNode.getChild("stopPhase", false);
		if (startNode != null)
			lifecycle.setStopPhase(getValueAsId(stopNode));

		fork.setLifecycle(lifecycle);
		return true;
	}

	protected boolean parseTargetPhases(final PlexusConfiguration phasesNode,
			final Consumer<TargetPhase> phaseConsumer) {
		if (phasesNode == null || phasesNode.getChildCount() == 0)
			return false;

		final PlexusConfiguration phaseNodes[] = phasesNode.getChildren("phase");
		if (phaseNodes.length > 0) {
			for (PlexusConfiguration phaseNode : phaseNodes)
				parseTargetPhase(phaseNode, phaseConsumer);
		}
		return true;
	}

	protected boolean parseTargetPhase(final PlexusConfiguration phaseNode, final Consumer<TargetPhase> phaseConsumer) {
		if (phaseNode == null)
			return false;

		final String id = getAttributeAsId(phaseNode, "id");
		if (isBlank(id))
			return false;

		final TargetPhase phase = new TargetPhase(id);
		phaseConsumer.accept(phase);

		parseTargetExecutions(phaseNode.getChild("executions", false), phase::addExecution);

		return true;
	}

	protected boolean parseTargetExecutions(final PlexusConfiguration executionsNode,
			final Consumer<String> execConsumer) {
		if (executionsNode == null || executionsNode.getChildCount() == 0)
			return false;

		final PlexusConfiguration execNodes[] = executionsNode.getChildren("execution");
		if (execNodes.length > 0) {
			for (PlexusConfiguration execNode : execNodes)
				parseTargetExecution(execNode, execConsumer);
		}
		return true;
	}

	protected boolean parseTargetExecution(final PlexusConfiguration execNode, final Consumer<String> execConsumer) {
		if (execNode == null)
			return false;

		final String id = getAttributeAsId(execNode, "id");
		if (isBlank(id))
			return false;

		execConsumer.accept(id);
		return true;
	}
}
