package net.runeduniverse.tools.maven.r4m.pem.view;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.plugin.descriptor.MojoDescriptor;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.api.pem.view.GoalView;

public class Goal implements GoalView {
	private String groupId;
	private String artifactId;
	private String goalId;
	private Fork fork = null;
	private Set<String> modes = new LinkedHashSet<>();

	private MojoDescriptor descriptor;

	public Goal(String groupId, String artifactId, String goalId) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.goalId = goalId;
	}

	@Override
	public String getGroupId() {
		return this.groupId;
	}

	@Override
	public String getArtifactId() {
		return this.artifactId;
	}

	@Override
	public String getGoalId() {
		return this.goalId;
	}

	@Override
	public Set<String> getModes() {
		return this.modes;
	}

	@Override
	public boolean hasFork() {
		return this.fork != null;
	}

	@Override
	public Fork getFork() {
		return this.fork;
	}

	@Override
	public MojoDescriptor getDescriptor() {
		return this.descriptor;
	}

	@Override
	public void addModes(String... modes) {
		for (int i = 0; i < modes.length; i++)
			this.modes.add(modes[i]);
	}

	@Override
	public void addModes(Collection<String> modes) {
		this.modes.addAll(modes);
	}

	@Override
	public void setFork(Fork fork) {
		this.fork = fork;
	}

	@Override
	public void setDescriptor(MojoDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO COMPARE GOALS
		return super.equals(obj);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("GoalView");

		tree.append("groupId", this.groupId)
				.append("artifactId", this.artifactId)
				.append("goalId", this.goalId);

		tree.append("modes", '[' + String.join(", ", this.modes) + ']');

		if (this.fork != null)
			tree.append(this.fork.toRecord());

		return tree;
	}
}
