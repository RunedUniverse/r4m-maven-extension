package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.plugin.descriptor.MojoDescriptor;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.Recordable;

public class Goal implements Recordable {
	private String groupId;
	private String artifactId;
	private String goalId;
	private Set<String> modes = new LinkedHashSet<>();
	private Set<Target> targets = new LinkedHashSet<>();

	private MojoDescriptor descriptor;

	public Goal() {
	}

	public Goal(String mvnGoalKey) {
		// TODO parse mvnGoalKey
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

	public Set<String> getModes() {
		return this.modes;
	}

	public Set<Target> getTargets() {
		return this.targets;
	}

	public MojoDescriptor getDescriptor() {
		return this.descriptor;
	}

	public Goal addModes(String... modes) {
		for (int i = 0; i < modes.length; i++)
			this.modes.add(modes[i]);
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO COMPARE GOALS
		return super.equals(obj);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("Goal");

		tree.append("groupId", this.groupId)
				.append("artifactId", this.artifactId)
				.append("goalId", this.goalId);

		tree.append("modes", '[' + String.join(", ", this.modes) + ']');

		// TODO list targets
		tree.append("targets", "// TODO list targets");

		return tree;
	}
}
