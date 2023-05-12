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
