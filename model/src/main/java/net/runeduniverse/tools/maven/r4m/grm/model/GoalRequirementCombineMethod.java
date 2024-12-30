/*
 * Copyright © 2024 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.grm.model;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.LinkedHashMap;
import java.util.Map;

public class GoalRequirementCombineMethod {

	@Deprecated
	public static final Map<String, GoalRequirementCombineMethod> KNOWN_VALUES = new LinkedHashMap<>(3);

	public static final GoalRequirementCombineMethod DEFAULT = new GoalRequirementCombineMethod("default");
	public static final GoalRequirementCombineMethod REPLACE = new GoalRequirementCombineMethod("replace");
	public static final GoalRequirementCombineMethod APPEND = new GoalRequirementCombineMethod("append");

	private final String key;

	protected GoalRequirementCombineMethod(final String key) {
		this.key = key;
		GoalRequirementCombineMethod.KNOWN_VALUES.put(key, this);
	}

	public String key() {
		return this.key;
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj;
	}

	@Override
	public String toString() {
		return this.key;
	}

	public static GoalRequirementCombineMethod create(String key) {
		if (isBlank(key))
			return null;

		key = key.trim()
				.toLowerCase();
		final GoalRequirementCombineMethod source = GoalRequirementCombineMethod.KNOWN_VALUES.get(key);
		if (source == null)
			return new GoalRequirementCombineMethod(key);
		else
			return source;
	}

	public static GoalRequirementCombineMethod get(final String key) {
		return GoalRequirementCombineMethod.KNOWN_VALUES.get(key);
	}
}
