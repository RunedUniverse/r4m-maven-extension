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
package net.runeduniverse.tools.maven.r4m.pem.model;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExecutionSource {

	@Deprecated
	public static final Map<String, ExecutionSource> KNOWN_SOURCES = new LinkedHashMap<>(4);

	public static final ExecutionSource OVERRIDE = new ExecutionSource("override");
	public static final ExecutionSource PACKAGING = new ExecutionSource("packaging");
	public static final ExecutionSource PLUGIN = new ExecutionSource("plugin");
	@Deprecated
	public static final ExecutionSource EFFECTIVE = new ExecutionSource("effective");

	private final String key;

	protected ExecutionSource(String key) {
		this.key = key;
		ExecutionSource.KNOWN_SOURCES.put(key, this);
	}

	public String key() {
		return this.key;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ExecutionSource))
			return false;

		return this.key.equals(((ExecutionSource) obj).key());
	}

	@Override
	public String toString() {
		return this.key;
	}

	public static ExecutionSource create(String key) {
		if (isBlank(key))
			return null;

		ExecutionSource source = ExecutionSource.KNOWN_SOURCES.get(key);
		if (source == null)
			return new ExecutionSource(key);
		else
			return source;
	}

}
