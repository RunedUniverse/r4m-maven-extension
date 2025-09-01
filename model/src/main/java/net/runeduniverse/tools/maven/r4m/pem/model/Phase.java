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
package net.runeduniverse.tools.maven.r4m.pem.model;

import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;
import static net.runeduniverse.lib.utils.common.HashUtils.hash;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;

public class Phase implements DataEntry {

	public static final String HINT = "phase";
	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.pem.model.Phase";

	protected final Supplier<List<Goal>> goalsSupplier;

	protected final List<Goal> goals;
	protected final String id;

	public Phase(final String id) {
		this(LinkedList::new, id);
	}

	public Phase(final Supplier<List<Goal>> goalsSupplier, final String id) {
		this.goalsSupplier = goalsSupplier;
		this.goals = this.goalsSupplier.get();
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public List<Goal> getGoals() {
		return this.goals;
	}

	public void addGoal(final Goal goal) {
		this.goals.add(goal);
	}

	public void addGoals(final Collection<Goal> goals) {
		this.goals.addAll(goals);
	}

	@Override
	public int hashCode() {
		return hash(type()) ^ hash(getId());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Phase))
			return false;
		final Phase phase = (Phase) obj;

		return objectEquals(this.id, phase.getId()) //
				&& objectEquals(this.goals, phase.getGoals());
	}

	@Override
	public Phase copy() {
		final Phase phase = new Phase(this.goalsSupplier, getId());

		phase.addGoals(getGoals());

		return phase;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("Phase");

		tree.append("id", this.id);

		for (Recordable goal : this.goals)
			tree.append(goal.toRecord());

		return tree;
	}
}
