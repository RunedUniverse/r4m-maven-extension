package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model;

import java.util.Set;

import org.apache.maven.plugin.descriptor.MojoDescriptor;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.Recordable;

public class Goal implements Recordable {
	private String groupId;
	private String artifactId;
	private String goalId;
	private Set<String> modes;
	private Set<Target> targets;

	private MojoDescriptor descriptor;

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
