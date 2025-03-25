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
package net.runeduniverse.tools.maven.r4m.pem.model;

import java.util.LinkedHashMap;
import java.util.Map;

import net.runeduniverse.lib.utils.common.api.Keyed;

import static net.runeduniverse.lib.utils.common.HashUtils.hash;
import static net.runeduniverse.lib.utils.common.ComparisonUtils.keyedEquals;
import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

/*
 * merge-procedure of sources
 *
 * 1. EFFECTIVE beats all other definitions if it exists!
 * 2. $pem = WORKFLOW merges with PACKAGING by replacing all lifecycles which are also defined in PACKAGING
 * 3. $pem = $pem merges with PLUGIN by replacing all goals which are also defined in PLUGIN
 * 4. $pem = OVERRIDE merges with $pem by replacing all lifecycles which are also defined in $pem
 */
public class ExecutionSource implements Keyed {

	@Deprecated
	public static final Map<String, ExecutionSource> KNOWN_SOURCES = new LinkedHashMap<>(4);

	public static final ExecutionSource OVERRIDE = new ExecutionSource("override");
	public static final ExecutionSource WORKFLOW = new ExecutionSource("workflow");
	public static final ExecutionSource PACKAGING = new ExecutionSource("packaging");
	public static final ExecutionSource PLUGIN = new ExecutionSource("plugin");
	@Deprecated
	public static final ExecutionSource EFFECTIVE = new ExecutionSource("effective");

	private final String key;

	protected ExecutionSource(final String key) {
		this.key = key;
		ExecutionSource.KNOWN_SOURCES.put(key, this);
	}

	@Override
	public String key() {
		return this.key;
	}

	@Override
	public int hashCode() {
		return hash(this.key);
	}

	@Override
	public boolean equals(final Object obj) {
		return keyedEquals(ExecutionSource.class, this, obj);
	}

	@Override
	public String toString() {
		return this.key;
	}

	public static ExecutionSource create(String key) {
		if (isBlank(key))
			return null;

		key = key.trim()
				.toLowerCase();
		return ExecutionSource.KNOWN_SOURCES.getOrDefault(key, new ExecutionSource(key));
	}
}
