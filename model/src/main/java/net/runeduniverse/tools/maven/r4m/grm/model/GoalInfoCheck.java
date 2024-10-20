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

import java.util.LinkedHashSet;

public class GoalInfoCheck extends AndCheck {

	protected final GroupIdCheck group = new GroupIdCheck();
	protected final ArtifactIdCheck artifact = new ArtifactIdCheck();
	protected final GoalCheck goal = new GoalCheck();

	public GoalInfoCheck() {
		super(new LinkedHashSet<>());
		this.conditions.add(this.group);
		this.conditions.add(this.artifact);
		this.conditions.add(this.goal);
	}

	public GoalInfoCheck(String groupId, String artifactId, String goalId) {
		this();
		this.group.setGroupId(groupId);
		this.artifact.setArtifactId(artifactId);
		this.goal.setGoal(goalId);
	}

	public String getGroupId() {
		return this.group.getGroupId();
	}

	public void setGroupId(String groupId) {
		this.group.setGroupId(groupId);
	}

	public String getArtifactId() {
		return this.artifact.getArtifactId();
	}

	public void setArtifactId(String artifactId) {
		this.artifact.setArtifactId(artifactId);
	}

	public String getGoalId() {
		return this.goal.getGoal();
	}

	public void setGoalId(String goalId) {
		this.goal.setGoal(goalId);
	}

	@Override
	public boolean isValid() {
		return this.group.isValid() && this.artifact.isValid() && this.goal.isValid();
	}
}
