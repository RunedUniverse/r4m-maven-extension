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
package net.runeduniverse.tools.maven.r4m.pem.view;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.plugin.descriptor.MojoDescriptor;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.view.api.GoalView;

public class Goal implements GoalView {

	private final Set<String> modes = new LinkedHashSet<>();

	private final String groupId;
	private final String artifactId;
	private final String goalId;

	private boolean optional;
	private Fork fork = null;
	private MojoDescriptor descriptor = null;

	public Goal(final String groupId, final String artifactId, final String goalId) {
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
	public void addModes(final String... modes) {
		for (String mode : modes)
			this.modes.add(mode);
	}

	@Override
	public void addModes(final Collection<String> modes) {
		this.modes.addAll(modes);
	}

	@Override
	public void setOptional(final boolean optional) {
		this.optional = optional;
	}

	@Override
	public void setFork(final Fork fork) {
		this.fork = fork;
	}

	@Override
	public void setDescriptor(final MojoDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof GoalView))
			return false;
		final GoalView view = (GoalView) obj;

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
		final CompoundTree tree = new DefaultCompoundTree("GoalView");

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
