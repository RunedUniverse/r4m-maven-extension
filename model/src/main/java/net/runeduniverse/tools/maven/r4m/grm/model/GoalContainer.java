/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
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

import java.util.Collection;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;

public class GoalContainer implements Recordable {

	protected final DataGroup matchData;
	protected final DataGroup prerequisiteData;
	protected final DataGroup dependentData;

	public GoalContainer(final DataGroup matchData, final DataGroup prerequisiteData, final DataGroup dependentData) {
		this.matchData = matchData;
		this.prerequisiteData = prerequisiteData;
		this.dependentData = dependentData;
	}

	public DataGroup getMatchGroup() {
		return this.matchData;
	}

	public Collection<DataEntry> getMatchEntries() {
		return this.matchData.getEntries();
	}

	public DataGroup getPrerequisiteGroup() {
		return this.prerequisiteData;
	}

	public Collection<DataEntry> getPrerequisiteEntries() {
		return this.prerequisiteData.getEntries();
	}

	public DataGroup getDependentGroup() {
		return this.dependentData;
	}

	public Collection<DataEntry> getDependentEntries() {
		return this.dependentData.getEntries();
	}

	public boolean addMatchEntry(final DataEntry entry) {
		return this.matchData.addEntry(entry);
	}

	public boolean addPrerequisiteEntry(final DataEntry entry) {
		return this.prerequisiteData.addEntry(entry);
	}

	public boolean addDependentEntry(final DataEntry entry) {
		return this.dependentData.addEntry(entry);
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("GoalContainer");
		CompoundTree subtree = ModelUtils.toRecord(this.matchData);
		if (subtree != null)
			tree.append(subtree);
		subtree = ModelUtils.toRecord(this.prerequisiteData);
		if (subtree != null)
			tree.append(subtree);
		subtree = ModelUtils.toRecord(this.dependentData);
		if (subtree != null)
			tree.append(subtree);
		return tree;
	}
}
