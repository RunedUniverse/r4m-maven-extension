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

public class GoalData implements DataEntry, Recordable {

	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.grm.model.GoalData";

	public static final String ERR_MSG_LOCKED_OBJECT = "Object is unmodifyable as it was already locked!";

	private boolean locked = false;

	protected String groupId = null;
	protected String id = null;
	protected String goalId = null;
	protected Integer hash = null;

	public String getGroupId() {
		return this.groupId;
	}

	public String getArtifactId() {
		return this.id;
	}

	public String getGoalId() {
		return this.goalId;
	}

	public GoalData setGroupId(String groupId) {
		if (this.locked)
			throw new UnsupportedOperationException(ERR_MSG_LOCKED_OBJECT);
		this.groupId = groupId;
		return this;
	}

	public GoalData setArtifactId(String artifactId) {
		if (this.locked)
			throw new UnsupportedOperationException(ERR_MSG_LOCKED_OBJECT);
		this.id = artifactId;
		return this;
	}

	public GoalData setGoalId(String goalId) {
		if (this.locked)
			throw new UnsupportedOperationException(ERR_MSG_LOCKED_OBJECT);
		this.goalId = goalId;
		return this;
	}

	public boolean isLocked() {
		return this.locked;
	}

	protected void lock() {
		this.locked = true;
	}

	@Override
	public DataEntry copy() {
		return new GoalData().setGroupId(getGroupId())
				.setArtifactId(getArtifactId())
				.setGoalId(getGoalId());
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = ModelUtils.createEntryTree(this);
		tree.append("groupId", getGroupId());
		tree.append("artifactId", getArtifactId());
		tree.append("goalId", getGoalId());
		return tree;
	}

	protected int _hashCode() {
		return hash(type()) ^ hash(getGroupId()) ^ hash(getArtifactId()) ^ hash(getGoalId());
	}

	@Override
	public int hashCode() {
		lock();
		if (this.hash == null)
			this.hash = _hashCode();
		return this.hash;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || hashCode() != obj.hashCode() || !(obj instanceof GoalData))
			return false;
		final GoalData other = (GoalData) obj;
		return strEquals(getGroupId(), other.getGroupId()) && //
				strEquals(getArtifactId(), other.getArtifactId()) && //
				strEquals(getGoalId(), other.getGoalId());
	}
}
