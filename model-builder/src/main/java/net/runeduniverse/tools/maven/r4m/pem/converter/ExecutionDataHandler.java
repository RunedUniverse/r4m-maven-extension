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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import net.runeduniverse.tools.maven.r4m.pem.converter.api.ConfigurationFactory;
import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataHandler;
import net.runeduniverse.tools.maven.r4m.pem.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;

@Component(role = DataHandler.class, hint = Execution.CANONICAL_NAME)
public class ExecutionDataHandler extends ADataHandler {

	@Override
	protected PlexusConfiguration toConfig(final ConfigurationFactory<PlexusConfiguration> factory,
			final DataEntry entry) {
		if (!(entry instanceof Execution))
			return null;

		final Execution data = (Execution) entry;
		final PlexusConfiguration cnf = factory.create(Execution.HINT);
		setAttributeAsId(cnf, "source", data.getSource()
				.key());
		setAttributeAsId(cnf, "id", data.getId());

		if (!data.isInherited())
			cnf.addChild("inherited", asId("false"));

		if (!data.getRestrictions()
				.isEmpty()) {
			final PlexusConfiguration restrictionNodes = cnf.getChild("restrictions", true);
			for (ExecutionRestriction restriction : data.getRestrictions())
				addConvertedEntry(restrictionNodes, factory, restriction);
		}

		{
			final PlexusConfiguration triggerNodes = factory.create("triggers");
			if (data.isAlwaysActive())
				triggerNodes.addChild("always", null);
			if (data.isDefaultActive())
				triggerNodes.addChild("default", null);
			if (data.isNeverActive())
				triggerNodes.addChild("never", null);
			for (ExecutionTrigger trigger : data.getTrigger())
				addConvertedEntry(triggerNodes, factory, trigger);
			if (0 < triggerNodes.getChildCount())
				cnf.addChild(triggerNodes);
		}

		{
			final PlexusConfiguration lifecycleNodes = cnf.getChild("lifecycles", true);
			for (Lifecycle lifecycle : data.getLifecycles()
					.values())
				addConvertedEntry(lifecycleNodes, factory, lifecycle);
		}

		return cnf;
	}
}
