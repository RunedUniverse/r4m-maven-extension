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

import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataFactory;
import net.runeduniverse.tools.maven.r4m.pem.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.PropertyRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.PropertyRestriction.MatchType;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = DataFactory.class, hint = ExecutionRestriction.CONTEXT + '>' + PropertyRestriction.HINT)
public class PropertyRestrictionDataFactory extends ADataFactory {

	@Override
	public DataEntry createEntry(final PlexusConfiguration cnf) {
		if (!PropertyRestriction.HINT.equals(cnf.getName()))
			return null;

		final String id = getAttributeAsId(cnf, "id", null);
		final boolean inverted = getAttributeAsBoolean(cnf, "inverted", false);
		final boolean exists = getAttributeAsBoolean(cnf, "exists", true);
		final String value = getAttributeAsId(cnf, "value", null);
		final MatchType type = asMatchType(cnf.getAttribute("match", null), MatchType.EQUALS);
		// skip invalid restriction
		if (isBlank(id))
			return null;
		return new PropertyRestriction(id, inverted, exists, value, type);
	}

	protected MatchType asMatchType(String value, final MatchType defaultValue) {
		if (value == null)
			return defaultValue;
		value = value.trim();
		final MatchType type = MatchType.get(value);
		if (type == null)
			return defaultValue;
		return type;
	}
}
