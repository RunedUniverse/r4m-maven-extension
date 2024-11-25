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

/*
 * merge-procedure of sources
 *
 * 1. EFFECTIVE beats all other definitions if it exists!
 * 2. $pem = WORKFLOW merges with PACKAGING by replacing all lifecycles which are also defined in PACKAGING
 * 3. $pem = $pem merges with PLUGIN by replacing all goals which are also defined in PLUGIN
 * 4. $pem = OVERRIDE merges with $pem by replacing all lifecycles which are also defined in $pem
 */
public class GoalRequirementSource {

	@Deprecated
	public static final Map<String, GoalRequirementSource> KNOWN_SOURCES = new LinkedHashMap<>(4);

	public static final GoalRequirementSource OVERRIDE = new GoalRequirementSource("override");
	public static final GoalRequirementSource WORKFLOW = new GoalRequirementSource("workflow");
	public static final GoalRequirementSource PACKAGING = new GoalRequirementSource("packaging");
	public static final GoalRequirementSource PLUGIN = new GoalRequirementSource("plugin");
	@Deprecated
	public static final GoalRequirementSource EFFECTIVE = new GoalRequirementSource("effective");

	private final String key;

	protected GoalRequirementSource(String key) {
		this.key = key;
		GoalRequirementSource.KNOWN_SOURCES.put(key, this);
	}

	public String key() {
		return this.key;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GoalRequirementSource))
			return false;

		return this.key.equals(((GoalRequirementSource) obj).key());
	}

	@Override
	public String toString() {
		return this.key;
	}

	public static GoalRequirementSource create(String key) {
		if (isBlank(key))
			return null;

		key = key.trim()
				.toLowerCase();
		final GoalRequirementSource source = GoalRequirementSource.KNOWN_SOURCES.get(key);
		if (source == null)
			return new GoalRequirementSource(key);
		else
			return source;
	}
}
