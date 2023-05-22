package net.runeduniverse.tools.maven.r4m.pem.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;

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
		for (int i = 0; i < modes.length; i++)
			this.modes.add(modes[i]);
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
		CompoundTree tree = new CompoundTree("Goal");

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
