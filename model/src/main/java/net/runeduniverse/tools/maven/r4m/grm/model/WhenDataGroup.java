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

import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;

import static net.runeduniverse.tools.maven.r4m.grm.model.ModelUtils.hash;

public class WhenDataGroup extends OrDataGroup implements Recordable {

	public static final String HINT = "when";
	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.grm.model.WhenDataGroup";

	protected boolean always = false;
	protected boolean never = false;

	public WhenDataGroup() {
		super(HINT);
	}

	protected WhenDataGroup(final String type) {
		super(type);
	}

	public boolean getAlwaysActive() {
		return this.always;
	}

	public boolean getNeverActive() {
		return this.never;
	}

	public void setAlwaysActive(boolean value) {
		this.always = value;
	}

	public void setNeverActive(boolean value) {
		this.never = value;
	}

	protected <T extends WhenDataGroup> T _copyDataTo(final T group) {
		group.setAlwaysActive(this.always);
		group.setNeverActive(this.never);
		return group;
	}

	@Override
	public DataGroup copy() {
		final WhenDataGroup group = new WhenDataGroup(type());
		_copyDataTo(group);
		return _copyEntriesTo(group);
	}

	@Override
	public boolean valid() {
		if (this.never)
			return true;
		return super.valid();
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = ModelUtils.createEntryTree(this);

		tree.append("always", Boolean.toString(this.always));
		tree.append("never", Boolean.toString(this.never));

		ModelUtils.appendEntries(tree, getEntries());
		return tree;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ hash(getAlwaysActive()) ^ hash(getNeverActive());
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj) || !(obj instanceof WhenDataGroup))
			return false;
		final WhenDataGroup data = (WhenDataGroup) obj;
		return getAlwaysActive() == data.getAlwaysActive() && getNeverActive() == data.getNeverActive();
	}
}
