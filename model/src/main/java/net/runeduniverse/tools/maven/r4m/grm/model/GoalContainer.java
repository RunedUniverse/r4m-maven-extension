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

import java.util.Collection;
import java.util.Collections;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;

public class GoalContainer implements Recordable {

	protected final DataGroup matchData;
	protected final DataGroup prerequisiteData;
	protected final DataGroup dependentData;

	public GoalContainer(final DataGroup matchData, final DataGroup prerequisiteData, final DataGroup dependentData) {
		this.matchData = matchData == null ? new DummyDataGroup() : matchData;
		this.prerequisiteData = prerequisiteData == null ? new DummyDataGroup() : prerequisiteData;
		this.dependentData = dependentData == null ? new DummyDataGroup() : dependentData;
	}

	public DataEntry getMatchEntry() {
		return this.matchData instanceof DummyDataGroup ? null : this.matchData;
	}

	public Collection<DataEntry> getMatchEntries() {
		return this.matchData.getEntries();
	}

	public DataEntry getPrerequisiteEntry() {
		return this.prerequisiteData instanceof DummyDataGroup ? null : this.prerequisiteData;
	}

	public Collection<DataEntry> getPrerequisiteEntries() {
		return this.prerequisiteData.getEntries();
	}

	public DataEntry getDependentEntry() {
		return this.dependentData instanceof DummyDataGroup ? null : this.dependentData;
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
		// TODO Auto-generated method stub
		return null;
	}

	protected class DummyDataGroup implements DataGroup {

		@Override
		public Collection<DataEntry> getEntries() {
			return Collections.emptySet();
		}

		@Override
		public boolean addEntry(DataEntry entry) {
			return false;
		}
	}
}
