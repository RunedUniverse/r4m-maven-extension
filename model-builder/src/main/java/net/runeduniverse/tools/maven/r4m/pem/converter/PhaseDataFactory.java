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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataFactory;
import net.runeduniverse.tools.maven.r4m.pem.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.pem.model.Phase;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = DataFactory.class, hint = Phase.HINT)
public class PhaseDataFactory extends ADataFactory {

	@Override
	public DataEntry createEntry(final PlexusConfiguration cnf) {
		if (!Phase.HINT.equals(cnf.getName()))
			return null;

		final String id = getAttributeAsId(cnf, "id");
		if (isBlank(id))
			return null;

		final Phase phase = new Phase(id);

		parseGoals(phase, cnf.getChild("goals", false));
		return phase;
	}

	protected boolean parseGoals(final Phase phase, final PlexusConfiguration nodeList) {
		if (nodeList == null || nodeList.getChildCount() == 0)
			return false;

		final PlexusConfiguration goalNodes[] = nodeList.getChildren("goal");
		if (goalNodes.length > 0) {
			for (PlexusConfiguration goalNode : goalNodes) {
				final DataEntry entry = convertEntry(goalNode);
				if (entry instanceof Goal)
					phase.addGoal((Goal) entry);
			}
		}
		return true;
	}
}
