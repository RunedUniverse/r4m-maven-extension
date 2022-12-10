package net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model;

import java.util.LinkedHashSet;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;

public class Phase {

	private final CompoundTree tree = new CompoundTree("Phase");
	private final String id;

	private final Set<Goal> goals;

	public Phase(final String id) {
		this.id = id;
		this.goals = new LinkedHashSet<>();
		this.tree.append("id", this.id);
	}

	public String getId() {
		return this.id;
	}

	public Set<Goal> getGoals() {
		return this.goals;
	}

	public void addGoal(Goal goal) {
		this.goals.add(goal);
		goal.toRecord(this.tree);
	}

	public void toRecord(CompoundTree tree) {
		tree.append(this.tree);
	}
}
