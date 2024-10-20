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
package net.runeduniverse.tools.maven.r4m.grm.check;

import java.util.Collection;
import net.runeduniverse.lib.utils.conditions.tools.ConditionIndexer;
import net.runeduniverse.lib.utils.conditions.tools.Entry;
import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;

public class GoalSelector implements Recordable {

	protected final GoalInfoCheck goalInfo = new GoalInfoCheck();
	protected final GoalOrderCheckSet dependencies = new GoalOrderCheckSet();
	protected final GoalOrderCheckSet dependents = new GoalOrderCheckSet();
	protected final Entry<EntityView> entry = new Entry<EntityView>(this.goalInfo, this.dependencies, this.dependents);

	public GoalSelector() {
	}

	public GoalSelector(String groupId, String artifactId, String goalId) {
		this.goalInfo.setGroupId(groupId);
		this.goalInfo.setArtifactId(artifactId);
		this.goalInfo.setGoalId(goalId);
	}

	public Entry<EntityView> asEntry() {
		return entry;
	}

	public String getGroupId() {
		return this.goalInfo.getGroupId();
	}

	public void setGroupId(String groupId) {
		this.goalInfo.setGroupId(groupId);
	}

	public String getArtifactId() {
		return this.goalInfo.getArtifactId();
	}

	public void setArtifactId(String artifactId) {
		this.goalInfo.setArtifactId(artifactId);
	}

	public String getGoalId() {
		return this.goalInfo.getGoalId();
	}

	public void setGoalId(String goalId) {
		this.goalInfo.setGoalId(goalId);
	}

	public GoalOrderCheckSet getDependencies() {
		return this.dependencies;
	}

	public GoalOrderCheckSet getDependents() {
		return this.dependents;
	}

	public void addDependency(GoalOrderCheck info) {
		this.dependencies.add(info);
	}

	public void addDependent(GoalOrderCheck info) {
		this.dependents.add(info);
	}

	public void addDependencies(Collection<GoalOrderCheck> infos) {
		for (GoalOrderCheck info : infos)
			this.dependencies.add(info);
	}

	public void addDependents(Collection<GoalOrderCheck> infos) {
		for (GoalOrderCheck info : infos)
			this.dependents.add(info);
	}

	@Override
	public CompoundTree toRecord() {
		final ConditionIndexer indexer = new ConditionIndexer();
		final CompoundTree tree = new CompoundTree("GoalSelector");

		indexer.toRecord(this.goalInfo);
		indexer.toRecord(this.dependencies);
		indexer.toRecord(this.dependents);

		return tree;
	}

}
