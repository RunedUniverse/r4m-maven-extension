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

public class ExecutionData implements DataEntry {

	public static final String HINT = "execution";
	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.grm.model.ExecutionData";

	protected String id = null;

	public String getId() {
		return this.id;
	}

	public ExecutionData setId(final String id) {
		this.id = id;
		return this;
	}

	@Override
	public DataEntry copy() {
		return new ExecutionData().setId(this.id);
	}
}
