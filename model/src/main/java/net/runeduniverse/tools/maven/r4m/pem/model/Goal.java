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

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;

public class Goal implements Recordable {

	private String groupId;
	private String artifactId;
	private String goalId;
	private Boolean optional = null;
	private Fork fork = null;

	private final Set<String> modes = new LinkedHashSet<>();

	public Goal() {
	}

	public Goal(String mvnGoalKey) {
		parseMvnGoalKey(mvnGoalKey);
	}

	public Goal(String groupId, String artifactId, String goalId) {
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

	public Goal addModes(String... modes) {
		for (String mode : modes)
			this.modes.add(mode);
		return this;
	}

	public Goal addModes(Collection<String> modes) {
		this.modes.addAll(modes);
		return this;
	}

	public Goal setOptional(Boolean optional) {
		this.optional = optional;
		return this;
	}

	public Goal setFork(Fork fork) {
		this.fork = fork;
		return this;
	}

	public boolean parseMvnGoalKey(String mvnGoalKey) {
		String[] keyValues = mvnGoalKey.split(":");

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
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Goal))
			return false;
		Goal goal = (Goal) obj;

		if (this.groupId == null) {
			if (goal.getGroupId() != null)
				return false;
		} else if (!this.groupId.equals(goal.getGroupId()))
			return false;

		if (this.artifactId == null) {
			if (goal.getArtifactId() != null)
				return false;
		} else if (!this.artifactId.equals(goal.getArtifactId()))
			return false;

		if (this.goalId == null) {
			if (goal.getGoalId() != null)
				return false;
		} else if (!this.goalId.equals(goal.getGoalId()))
			return false;

		if (!(this.modes.size() == goal.getModes()
				.size() && this.modes.containsAll(goal.getModes())))
			return false;

		if (this.optional == null) {
			if (goal.getOptional() != null)
				return false;
		} else if (!this.optional.equals(goal.getOptional()))
			return false;

		if (this.fork != null)
			return this.fork.equals(goal.getFork());

		return true;
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new DefaultCompoundTree("Goal");

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
