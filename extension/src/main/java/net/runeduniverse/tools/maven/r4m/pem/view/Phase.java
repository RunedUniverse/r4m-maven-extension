package net.runeduniverse.tools.maven.r4m.pem.view;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.api.Recordable;
import net.runeduniverse.tools.maven.r4m.pem.view.api.GoalView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.PhaseView;

public class Phase implements PhaseView {

	private final String id;
	private final List<GoalView> goals = new LinkedList<>();

	public Phase(String id) {
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
	public void addGoal(GoalView goal) {
		if (goal == null)
			return;
		this.goals.add(goal);
	}

	@Override
	public void removeGoal(GoalView goal) {
		this.goals.remove(goal);
	}

	@Override
	public void addNewGoals(Collection<GoalView> goals) {
		for (GoalView goal : goals)
			if (!this.goals.contains(goal))
				addGoal(goal);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("PhaseView");

		tree.append("id", this.id);

		for (Recordable goal : this.goals)
			tree.append(goal.toRecord());

		return tree;
	}

}
