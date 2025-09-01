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

import static net.runeduniverse.lib.utils.common.HashUtils.hash;

import java.util.Collection;
import java.util.function.Supplier;

public abstract class ADataGroup<C extends Collection<DataEntry>> implements DataGroup {

	private final String type;
	protected final C collection;

	protected ADataGroup(final Supplier<C> supplier) {
		this(null, supplier);
	}

	protected ADataGroup(final String type, final Supplier<C> supplier) {
		this.type = type;
		this.collection = supplier.get();
	}

	@Override
	public Collection<DataEntry> getEntries() {
		return this.collection;
	}

	@Override
	public boolean addEntry(final DataEntry entry) {
		return this.collection.add(entry);
	}

	@Override
	public String type() {
		if (this.type == null)
			return DataGroup.super.type();
		return this.type;
	}

	protected <T extends ADataGroup<C>> T _copy(final T group) {
		return _copyEntriesTo(group);
	}

	protected <T extends ADataGroup<C>> T _copyEntriesTo(final T group) {
		for (DataEntry d : this.collection)
			group.addEntry(d.copy());
		return group;
	}

	@Override
	public int hashCode() {
		return hash(type());
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || hashCode() != obj.hashCode() || !(obj instanceof ADataGroup))
			return false;
		final ADataGroup<?> other = (ADataGroup<?>) obj;
		final Collection<DataEntry> colA = getEntries();
		final Collection<DataEntry> colB = other.getEntries();
		if (colA.size() != colB.size())
			return false;
		return colB.containsAll(colA);
	}
}
