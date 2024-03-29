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
package net.runeduniverse.tools.maven.r4m.pem.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;

public class Phase implements Recordable {

	private String id;

	private List<Goal> goals = new LinkedList<>();

	public Phase(final String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public List<Goal> getGoals() {
		return this.goals;
	}

	public void addGoal(Goal goal) {
		this.goals.add(goal);
	}

	public void addGoals(Collection<Goal> goals) {
		this.goals.addAll(goals);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("Phase");

		tree.append("id", this.id);

		for (Recordable goal : this.goals)
			tree.append(goal.toRecord());

		return tree;
	}

}
