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
package net.runeduniverse.tools.maven.r4m.lifecycle.api;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

public interface TaskData {

	public String[] getExecutions();

	public String[] getModes();

	public default String getPrimaryExecutionOrDefault(String other) {
		final String[] e = getExecutions();
		if (e == null)
			return other;
		for (String s : e)
			if (!isBlank(s))
				return s;
		return other;
	}

	public default boolean hasValidModes() {
		for (String mode : getModes())
			if (!isBlank(mode))
				return true;
		return false;
	}

}
