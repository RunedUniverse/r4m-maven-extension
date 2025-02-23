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

import static net.runeduniverse.tools.maven.r4m.grm.model.ModelUtils.hash;
import static net.runeduniverse.tools.maven.r4m.grm.model.ModelUtils.strEquals;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;

public class LifecycleData extends ADataEntry implements DataEntry, Recordable {

	public static final String HINT = "lifecycle";
	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.grm.model.LifecycleData";

	protected String id = null;

	public LifecycleData() {
		super(HINT);
	}

	public String getId() {
		return this.id;
	}

	public LifecycleData setId(final String id) {
		this.id = id;
		return this;
	}

	@Override
	public DataEntry copy() {
		return new LifecycleData().setId(this.id);
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = ModelUtils.createEntryTree(this);
		tree.append("id", getId());
		return tree;
	}

	@Override
	public int hashCode() {
		return hash(type()) ^ hash(getId());
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || hashCode() != obj.hashCode() || !(obj instanceof LifecycleData))
			return false;
		final LifecycleData other = (LifecycleData) obj;
		return strEquals(getId(), other.getId());
	}
}
