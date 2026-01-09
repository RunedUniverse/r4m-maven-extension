/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
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

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import net.runeduniverse.tools.maven.r4m.pem.converter.api.ConfigurationFactory;
import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataHandler;
import net.runeduniverse.tools.maven.r4m.pem.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetPhase;

@Component(role = DataHandler.class, hint = Fork.CANONICAL_NAME)
public class ForkDataHandler extends ADataHandler {

	@Override
	protected PlexusConfiguration toConfig(final ConfigurationFactory<PlexusConfiguration> factory,
			final DataEntry entry) {
		if (!(entry instanceof Fork))
			return null;

		final Fork data = (Fork) entry;
		final PlexusConfiguration cnf = factory.create(Fork.HINT);

		if (!data.getExecutions()
				.isEmpty()) {
			final PlexusConfiguration executionNodes = cnf.getChild("executions", true);
			for (String execution : data.getExecutions())
				setAttributeAsId(executionNodes.getChild("execution", true), "id", execution);
		}

		if (!isBlank(data.getMode()))
			setAttributeAsId(cnf.getChild("mode", true), "id", data.getMode());

		if (data.getPhases() != null && !data.getPhases()
				.isEmpty()) {
			final PlexusConfiguration phaseNodes = cnf.getChild("phases", true);
			for (TargetPhase phase : data.getPhases())
				phaseNodes.addChild(convert(factory, phase));
		}

		if (!data.getExcludedPhases()
				.isEmpty()) {
			final PlexusConfiguration excludedPhaseNodes = cnf.getChild("excludedPhases", true);
			for (TargetPhase phase : data.getExcludedPhases())
				excludedPhaseNodes.addChild(convert(factory, phase));
		}

		if (data.getLifecycle() != null)
			cnf.addChild(convert(factory, data.getLifecycle()));

		return cnf;
	}

	public PlexusConfiguration convert(final ConfigurationFactory<PlexusConfiguration> factory,
			final TargetPhase targetPhase) {
		final PlexusConfiguration node = factory.create("phase");
		setAttributeAsId(node, "id", targetPhase.getId());

		if (!targetPhase.getExecutions()
				.isEmpty()) {
			final PlexusConfiguration executionNodes = node.getChild("executions", true);
			for (String execution : targetPhase.getExecutions())
				setAttributeAsId(executionNodes.getChild("execution", true), "id", execution);
		}

		return node;
	}

	public PlexusConfiguration convert(final ConfigurationFactory<PlexusConfiguration> factory,
			final TargetLifecycle targetLifecycle) {
		final PlexusConfiguration node = factory.create("lifecycle");
		setAttributeAsId(node, "id", targetLifecycle.getId());

		if (!isBlank(targetLifecycle.getStartPhase()))
			node.addChild("startPhase", asId(targetLifecycle.getStartPhase()));
		if (!isBlank(targetLifecycle.getStopPhase()))
			node.addChild("stopPhase", asId(targetLifecycle.getStopPhase()));

		return node;
	}
}
