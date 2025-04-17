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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;

import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;

public class Goal implements DataEntry {

	public static final String HINT = "goal";
	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.pem.model.Goal";

	protected final Supplier<Set<String>> modesSupplier;

	protected final Set<String> modes;

	protected String groupId;
	protected String artifactId;
	protected String goalId;
	protected Boolean optional = null;
	protected Fork fork = null;

	public Goal() {
		this(null, null, null);
	}

	public Goal(final String mvnGoalKey) {
		this(LinkedHashSet::new, mvnGoalKey);
	}

	public Goal(final Supplier<Set<String>> modesSupplier, final String mvnGoalKey) {
		this.modesSupplier = modesSupplier;
		this.modes = this.modesSupplier.get();
		parseMvnGoalKey(mvnGoalKey);
	}

	public Goal(final String groupId, final String artifactId, final String goalId) {
		this(LinkedHashSet::new, groupId, artifactId, goalId);
	}

	public Goal(final Supplier<Set<String>> modesSupplier, final String groupId, final String artifactId,
			final String goalId) {
		this.modesSupplier = modesSupplier;
		this.modes = this.modesSupplier.get();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.goalId = goalId;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public String getGoalId() {
		return this.goalId;
	}

	public Boolean getOptional() {
		return this.optional;
	}

	public Set<String> getModes() {
		return this.modes;
	}

	public boolean hasFork() {
		return this.fork != null;
	}

	public Fork getFork() {
		return this.fork;
	}

	public Goal addModes(final String... modes) {
		for (String mode : modes)
			this.modes.add(mode);
		return this;
	}

	public Goal addModes(final Collection<String> modes) {
		this.modes.addAll(modes);
		return this;
	}

	public Goal setOptional(final Boolean optional) {
		this.optional = optional;
		return this;
	}

	public Goal setFork(final Fork fork) {
		this.fork = fork;
		return this;
	}

	public boolean parseMvnGoalKey(final String mvnGoalKey) {
		final String[] keyValues = mvnGoalKey.split(":");

		switch (keyValues.length) {
		case 2:
			// prefix:goal
			// prefix
			this.goalId = keyValues[1];
			return true;
		case 3:
			// groupId:artifactId:goal
			this.groupId = keyValues[0];
			this.artifactId = keyValues[1];
			this.goalId = keyValues[2];
			return true;
		case 4:
			// groupId:artifactId:version:goal
			this.groupId = keyValues[0];
			this.artifactId = keyValues[1];
			// version
			this.goalId = keyValues[3];
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Goal))
			return false;
		final Goal goal = (Goal) obj;

		if (!(this.modes.size() == goal.getModes()
				.size() && this.modes.containsAll(goal.getModes())))
			return false;

		return objectEquals(this.groupId, goal.getGroupId()) //
				&& objectEquals(this.artifactId, goal.getArtifactId()) //
				&& objectEquals(this.goalId, goal.getGoalId()) //
				&& objectEquals(this.optional, goal.getOptional()) //
				&& objectEquals(this.fork, goal.getFork());
	}

	@Override
	public Goal copy() {
		final Goal goal = new Goal(this.modesSupplier, getGroupId(), getArtifactId(), getGoalId());

		goal.setOptional(getOptional());
		goal.addModes(getModes());
		goal.setFork(getFork());

		return goal;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("Goal");

		tree.append("groupId", this.groupId)
				.append("artifactId", this.artifactId)
				.append("goalId", this.goalId);

		tree.append("modes", '[' + String.join(", ", this.modes) + ']');

		if (this.optional != null)
			tree.append("optional", this.optional.toString());

		if (this.fork != null)
			tree.append(this.fork.toRecord());

		return tree;
	}
}
