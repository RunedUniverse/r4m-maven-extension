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
package net.runeduniverse.tools.maven.r4m.pem.writer.restrictions;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionRestrictionWriter;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.PropertyRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.PropertyRestriction.MatchType;

@Component(role = ExecutionRestrictionWriter.class, hint = PropertyRestriction.CANONICAL_NAME)
public class PropertyRestrictionWriter implements ExecutionRestrictionWriter {

	@Override
	public boolean append(final PlexusConfiguration restrictionsNode, final ExecutionRestriction restriction) {
		if (restriction == null || !(restriction instanceof PropertyRestriction))
			return false;
		final PropertyRestriction rest = (PropertyRestriction) restriction;
		final PlexusConfiguration node = restrictionsNode.getChild(PropertyRestriction.HINT, true);

		setAttribute(node, "id", rest.getId());
		setAttributeSkipDefault(node, "inverted", rest.getInverted(), false);
		setAttributeSkipDefault(node, "exists", rest.getExists(), true);
		setAttribute(node, "value", rest.getValue());
		setAttributeSkipDefault(node, "match", rest.getMatchType(), MatchType.EQUALS);

		return true;
	}

	protected void setAttribute(final PlexusConfiguration node, final String name, final String value) {
		if (value == null)
			return;
		node.setAttribute(name, value);
	}

	protected void setAttributeSkipDefault(final PlexusConfiguration node, final String name, final Object value,
			final Object defaultValue) {
		if (value == null)
			return;
		if (defaultValue != null && defaultValue.equals(value))
			return;
		node.setAttribute(name, value.toString());
	}
}
