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
package net.runeduniverse.tools.maven.r4m.geom.model;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;

public class GoalOrderInfo implements Recordable {

	private String groupId = null;
	private String artifactId = null;
	private String goalId = null;
	private ExecutionSource source = null;
	private WhenCheck when = new WhenCheck();

	public GoalOrderInfo() {
	}

	public GoalOrderInfo(String groupId, String artifactId, String goalId) {
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

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("GoalOrderInfo");

		tree.append("groupId", this.groupId);
		tree.append("artifactId", this.artifactId);
		tree.append("goalId", this.goalId);
		tree.append("source", this.source.key());

		if (this.when != null)
			tree.append(this.when.toRecord());

		return tree;
	}

}
