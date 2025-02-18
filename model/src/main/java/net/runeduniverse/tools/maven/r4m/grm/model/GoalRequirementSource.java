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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

/*
 * merge-procedure of sources
 *
 * 1. EFFECTIVE beats all other definitions if it exists!
 * 2. $grm = WORKFLOW merges with PACKAGING by replacing (configurable!) all goals (prereq/dependencies) which are also defined in PACKAGING
 * 3. $grm = $grm merges with PLUGIN by replacing all goals which are also defined in PLUGIN, required flag must match for replace and remove!
 * 4. $grm = OVERRIDE merges with $grm by replacing (configurable!) all goals which are also defined in $grm
 */
public class GoalRequirementSource {

	@Deprecated
	private static final Map<String, GoalRequirementSource> KNOWN_SOURCES = new LinkedHashMap<>(4);
	@Deprecated
	private static final NavigableSet<Integer> KNOWN_PRIORITIES = new TreeSet<>();

	public static final GoalRequirementSource OVERRIDE = //
			new GoalRequirementSource("override", GoalRequirementCombineMethod.REPLACE, 3);
	public static final GoalRequirementSource WORKFLOW = //
			new GoalRequirementSource("workflow", GoalRequirementCombineMethod.REPLACE, 2);
	public static final GoalRequirementSource PACKAGING = //
			new GoalRequirementSource("packaging", null, 1);
	public static final GoalRequirementSource PLUGIN = //
			new GoalRequirementSource("plugin", null, 0);
	@Deprecated
	public static final GoalRequirementSource EFFECTIVE = //
			new GoalRequirementSource("effective", null, Integer.MAX_VALUE);

	private final String key;

	private final GoalRequirementCombineMethod defaultCombineMethod;

	private final int priority;

	protected GoalRequirementSource(final String key, final GoalRequirementCombineMethod defaultCombineMethod,
			final int priority) {
		this.key = key;
		this.defaultCombineMethod = defaultCombineMethod;
		this.priority = priority;
		GoalRequirementSource.KNOWN_SOURCES.put(key, this);
		GoalRequirementSource.KNOWN_PRIORITIES.add(this.priority);
	}

	public String key() {
		return this.key;
	}

	public GoalRequirementCombineMethod defaultCombineMethod() {
		return this.defaultCombineMethod;
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj;
	}

	@Override
	public String toString() {
		return this.key;
	}

	public static GoalRequirementSource create(String key, final GoalRequirementCombineMethod defaultCombineMethod,
			final int priority) {
		if (isBlank(key))
			return null;

		key = key.trim()
				.toLowerCase();
		final GoalRequirementSource source = GoalRequirementSource.KNOWN_SOURCES.get(key);
		if (source == null)
			return new GoalRequirementSource(key, defaultCombineMethod, priority);
		else
			return source;
	}

	public static GoalRequirementSource get(final String key) {
		return GoalRequirementSource.KNOWN_SOURCES.get(key);
	}

	public static GoalRequirementCombineMethod getValue(final GoalRequirementSource source,
			final GoalRequirementCombineMethod method) {
		if (source == null)
			return null;
		final GoalRequirementCombineMethod m = source.defaultCombineMethod();
		// check if the default value is null, then this source is not supported
		if (m == null)
			return null;
		if (method == GoalRequirementCombineMethod.DEFAULT)
			return m;
		return method;
	}

	public static int getPriority(final GoalRequirementSource source, final boolean required) {
		if (required)
			return GoalRequirementSource.KNOWN_PRIORITIES.last() + source.priority;
		return source.priority;
	}
}
