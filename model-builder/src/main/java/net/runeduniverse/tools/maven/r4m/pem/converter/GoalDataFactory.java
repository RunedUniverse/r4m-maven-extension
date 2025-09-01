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
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Component(role = DataFactory.class, hint = Goal.HINT)
public class GoalDataFactory extends ADataFactory {

	@Override
	public DataEntry createEntry(final PlexusConfiguration cnf) {
		if (!Goal.HINT.equals(cnf.getName()))
			return null;

		final String groupId = getValueAsId(cnf.getChild("groupId"));
		final String artifactId = getValueAsId(cnf.getChild("artifactId"));
		final String goalId = getAttributeAsId(cnf, "id");
		if (isBlank(groupId) || isBlank(artifactId) || isBlank(goalId))
			return null;

		final String optionalValue = getAttributeAsId(cnf, "optional");
		final Boolean optional = isBlank(optionalValue) ? null : Boolean.parseBoolean(optionalValue);

		final Goal goal = new Goal(groupId, artifactId, goalId).setOptional(optional);

		{
			final Set<String> modes = new LinkedHashSet<String>(1) {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean add(String e) {
					if (isBlank(e))
						return false;
					return super.add(e);
				}
			};

			parseModes(modes, cnf.getChild("modes", false));

			// no modes selected!
			if (modes.isEmpty())
				return null;
			goal.addModes(modes);
		}

		{
			final DataEntry entry = convertEntry(cnf.getChild("fork", false));
			if (entry instanceof Fork)
				goal.setFork((Fork) entry);
		}

		return goal;
	}

	protected boolean parseModes(final Collection<String> modes, final PlexusConfiguration modesNode) {
		if (modesNode == null)
			return false;
		for (PlexusConfiguration modeNode : modesNode.getChildren())
			parseMode(modes, modeNode);
		return true;
	}

	protected boolean parseMode(final Collection<String> modes, final PlexusConfiguration modeNode) {
		if (modeNode == null)
			return false;

		final String name = modeNode.getName();
		if (isBlank(name))
			return false;

		final String id = getAttributeAsId(modeNode, "id");
		if (id == null) {
			modes.add(name.trim());
			return true;
		}
		if (isBlank(id) || !name.equals("mode"))
			return false;

		modes.add(id.trim());
		return true;
	}
}
