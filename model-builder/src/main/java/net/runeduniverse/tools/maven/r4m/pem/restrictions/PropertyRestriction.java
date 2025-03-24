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
package net.runeduniverse.tools.maven.r4m.pem.restrictions;

import java.util.Properties;
import java.util.regex.Pattern;

import net.runeduniverse.lib.utils.common.api.Keyed;
import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;

import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;
import static net.runeduniverse.lib.utils.common.StringUtils.strEquals;
import static net.runeduniverse.lib.utils.common.StringUtils.strEqualsIgnoreCase;

public class PropertyRestriction implements ExecutionRestriction<ExecutionArchiveSelectorConfig> {

	public static final String HINT = "property";

	protected final String id;
	protected final boolean inverted;
	protected final boolean exists;
	protected final String value;
	protected final MatchType matchType;

	public PropertyRestriction(final String id, final boolean inverted, final boolean exists, final String value,
			final MatchType matchType) {
		this.id = id;
		this.inverted = inverted;
		this.exists = exists;
		this.value = value;
		this.matchType = matchType;
	}

	public String getId() {
		return this.id;
	}

	public boolean getInverted() {
		return this.inverted;
	}

	public boolean getExists() {
		return this.exists;
	}

	public String getValue() {
		return this.value;
	}

	public MatchType getMatchType() {
		return this.matchType;
	}

	@Override
	public String getHint() {
		return PropertyRestriction.HINT;
	}

	@Override
	public Class<ExecutionArchiveSelectorConfig> getDataType() {
		return ExecutionArchiveSelectorConfig.class;
	}

	@Override
	public boolean isActive(final ExecutionArchiveSelectorConfig config) {
		final Properties properties = config.getProperties();
		if (this.inverted)
			return !_isActive(properties);
		return _isActive(properties);
	}

	protected boolean _isActive(final Properties properties) {
		final boolean exists = properties.containsKey(this.id);

		if (this.exists == false) {
			return exists == false;
		}
		if (exists == false)
			return false;
		if (this.value == null)
			return true;

		final String value = properties.getProperty(this.id);

		switch (this.matchType) {
		case EQUALS:
			return strEquals(this.value, value);
		case EQUALS_IGNORE_CASE:
			return strEqualsIgnoreCase(this.value, value);
		case REGEX:
			return Pattern.matches(this.value, value);
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PropertyRestriction))
			return false;
		final PropertyRestriction restriction = (PropertyRestriction) obj;

		return objectEquals(this.id, restriction.getId()) && objectEquals(this.inverted, restriction.getInverted())
				&& objectEquals(this.exists, restriction.exists) && objectEquals(this.value, restriction.getValue())
				&& objectEquals(this.matchType, restriction.getMatchType());
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree(HINT);
		tree.append("id", this.id);
		tree.append("inverted", Boolean.toString(this.inverted));
		tree.append("exists", Boolean.toString(this.exists));
		tree.append("value", this.value);
		tree.append("match", MatchType.toString(this.matchType));
		return tree;
	}

	public static enum MatchType implements Keyed {

		EQUALS("equals"), EQUALS_IGNORE_CASE("equals-ignore-case"), REGEX("regex");

		private final String key;

		private MatchType(final String key) {
			this.key = key;
		}

		@Override
		public String key() {
			return this.key;
		}

		public static MatchType get(String value) {
			if (value == null)
				return null;
			value = value.trim();
			switch (value) {
			case "equals":
				return EQUALS;
			case "equals-ignore-case":
				return EQUALS_IGNORE_CASE;
			case "regex":
				return REGEX;
			}
			return null;
		}

		public static String toString(final MatchType type) {
			if (type == null)
				return "null";
			return type.key();
		}
	}
}
