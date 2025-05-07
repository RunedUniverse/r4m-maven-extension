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

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;

import static net.runeduniverse.lib.utils.common.HashUtils.hash;
import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;

public class DisableSuperPomOverride implements ModelOverride {

	public static final String TYPE = "no-super-pom";
	public static final String HINT = "disable-super-pom";
	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.pem.model.DisableSuperPomOverride";

	protected boolean active;

	public DisableSuperPomOverride(final boolean active) {
		this.active = active;
	}

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	@Override
	public void setActive(final boolean active) {
		this.active = active;
	}

	@Override
	public int hashCode() {
		return hash(HINT);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DisableSuperPomOverride))
			return false;
		final DisableSuperPomOverride override = (DisableSuperPomOverride) obj;

		return objectEquals(type(), override.type()) && //
				objectEquals(this.active, override.isActive());
	}

	@Override
	public DisableSuperPomOverride copy() {
		final DisableSuperPomOverride override = new DisableSuperPomOverride(isActive());
		return override;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree(HINT);
		tree.append("active", Boolean.toString(this.active));
		return tree;
	}
}
