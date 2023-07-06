package net.runeduniverse.tools.maven.r4m.pem.view;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.plugin.descriptor.MojoDescriptor;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.view.api.GoalView;

public class Goal implements GoalView {

	private String groupId;
	private String artifactId;
	private String goalId;
	private boolean optional;
	private Fork fork = null;

	private final Set<String> modes = new LinkedHashSet<>();

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
	public boolean isOptional() {
		return this.optional;
	}

	@Override
	public boolean hasFork() {
		return this.fork != null;
	}

	@Override
	public boolean hasValidFork() {
		return this.fork != null && this.fork.isValid();
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
		for (String mode : modes)
			this.modes.add(mode);
	}

	@Override
	public void addModes(Collection<String> modes) {
		this.modes.addAll(modes);
	}

	@Override
	public void setOptional(boolean optional) {
		this.optional = optional;
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
		if (this == obj)
			return true;

		if (!(obj instanceof GoalView))
			return false;
		GoalView view = (GoalView) obj;

		if (!(this.groupId.equals(view.getGroupId()) && this.artifactId.equals(view.getArtifactId())
				&& this.goalId.equals(view.getGoalId()))
				|| !(this.modes.size() == view.getModes()
						.size() && this.modes.containsAll(view.getModes())))
			return false;

		if (this.fork != null)
			return this.fork.equals(view.getFork());

		return true;
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("GoalView");

		tree.append("groupId", this.groupId)
				.append("artifactId", this.artifactId)
				.append("goalId", this.goalId);

		tree.append("modes", '[' + String.join(", ", this.modes) + ']');
		tree.append("optional", Boolean.toString(this.optional));

		if (this.fork != null)
			tree.append(this.fork.toRecord());

		return tree;
	}

}
