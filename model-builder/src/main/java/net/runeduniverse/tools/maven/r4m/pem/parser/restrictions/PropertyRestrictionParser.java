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
package net.runeduniverse.tools.maven.r4m.pem.parser.restrictions;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionRestrictionParser;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.restrictions.PropertyRestriction;
import net.runeduniverse.tools.maven.r4m.pem.restrictions.PropertyRestriction.MatchType;

@Component(role = ExecutionRestrictionParser.class, hint = PropertyRestriction.HINT)
public class PropertyRestrictionParser implements ExecutionRestrictionParser {

	@Override
	public ExecutionRestriction<ExecutionArchiveSelectorConfig> parse(final PlexusConfiguration node) {
		final String id = asId(node.getAttribute("id", null));
		final boolean inverted = asBoolean(node.getAttribute("inverted", null), false);
		final boolean exists = asBoolean(node.getAttribute("exists", null), true);
		final String value = asId(node.getAttribute("value", null));
		final MatchType type = asMatchType(node.getAttribute("match", null), MatchType.EQUALS);
		return new PropertyRestriction(id, inverted, exists, value, type);
	}

	protected String asId(final String value) {
		if (value == null)
			return value;
		return value.trim();
	}

	protected boolean asBoolean(String value, final boolean defaultValue) {
		if (value == null)
			return defaultValue;
		value = value.trim();
		if ("true".equalsIgnoreCase(value))
			return true;
		if ("false".equalsIgnoreCase(value))
			return false;
		return defaultValue;
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
