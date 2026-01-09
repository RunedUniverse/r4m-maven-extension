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
package net.runeduniverse.tools.maven.r4m.pem.restrictions;

import java.util.Properties;
import java.util.regex.Pattern;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionRestrictionEvaluationHandler;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.PropertyRestriction;

import static net.runeduniverse.lib.utils.common.StringUtils.strEquals;
import static net.runeduniverse.lib.utils.common.StringUtils.strEqualsIgnoreCase;

@Component(role = ExecutionRestrictionEvaluationHandler.class, hint = PropertyRestriction.CANONICAL_NAME)
public class PropertyRestrictionHandler implements ExecutionRestrictionEvaluationHandler {

	@Override
	public boolean isActive(ExecutionArchiveSelectorConfig config, ExecutionRestriction entry) {
		if (!(entry instanceof PropertyRestriction))
			return false;
		final PropertyRestriction restriction = (PropertyRestriction) entry;
		final Properties properties = config.getProperties();
		if (restriction.getInverted())
			return !_isActive(restriction, properties);
		return _isActive(restriction, properties);
	}

	protected boolean _isActive(final PropertyRestriction restriction, final Properties properties) {
		final boolean exists = properties.containsKey(restriction.getId());

		if (restriction.getExists() == false) {
			return exists == false;
		}
		if (exists == false)
			return false;
		if (restriction.getValue() == null)
			return true;

		final String value = properties.getProperty(restriction.getId());

		switch (restriction.getMatchType()) {
		case EQUALS:
			return strEquals(restriction.getValue(), value);
		case EQUALS_IGNORE_CASE:
			return strEqualsIgnoreCase(restriction.getValue(), value);
		case REGEX:
			return Pattern.matches(restriction.getValue(), value);
		}
		return false;
	}
}
