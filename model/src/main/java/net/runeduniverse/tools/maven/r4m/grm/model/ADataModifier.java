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
package net.runeduniverse.tools.maven.r4m.grm.model;

import static net.runeduniverse.tools.maven.r4m.grm.model.ModelUtils.hash;

public abstract class ADataModifier implements DataModifier {

	private final String type;

	protected DataEntry entry = null;

	protected ADataModifier() {
		this(null);
	}

	protected ADataModifier(final String type) {
		this.type = type;
	}

	@Override
	public DataEntry getEntry() {
		return this.entry;
	}

	@Override
	public boolean setEntry(final DataEntry entry) {
		this.entry = entry;
		return true;
	}

	@Override
	public String type() {
		if (this.type == null)
			return DataModifier.super.type();
		return this.type;
	}

	protected <T extends ADataModifier> T _copy(final T group) {
		return _copyEntryTo(group);
	}

	protected <T extends ADataModifier> T _copyEntryTo(final T group) {
		group.setEntry(this.entry.copy());
		return group;
	}

	@Override
	public int hashCode() {
		return hash(type());
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || hashCode() != obj.hashCode() || !(obj instanceof ADataModifier))
			return false;
		final ADataModifier other = (ADataModifier) obj;
		final DataEntry entryA = getEntry(), entryB = other.getEntry();
		if (entryA == entryB)
			return true;
		if (entryA == null || entryB == null)
			return false;
		return entryA.equals(entryB);
	}
}
