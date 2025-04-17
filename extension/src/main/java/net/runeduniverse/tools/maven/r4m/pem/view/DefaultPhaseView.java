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
import java.util.LinkedList;
import java.util.List;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;
import net.runeduniverse.tools.maven.r4m.pem.view.api.GoalView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.PhaseView;

public class DefaultPhaseView implements PhaseView {

	private final List<GoalView> goals = new LinkedList<>();
	private final String id;

	public DefaultPhaseView(final String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public List<GoalView> getGoals() {
		return this.goals;
	}

	@Override
	public void addGoal(final GoalView goal) {
		if (goal == null)
			return;
		this.goals.add(goal);
	}

	@Override
	public void removeGoal(final GoalView goal) {
		this.goals.remove(goal);
	}

	@Override
	public void addNewGoals(final Collection<GoalView> goals) {
		for (GoalView goal : goals)
			if (!this.goals.contains(goal))
				addGoal(goal);
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("PhaseView");

		tree.append("id", this.id);

		for (Recordable goal : this.goals)
			tree.append(goal.toRecord());

		return tree;
	}

}
