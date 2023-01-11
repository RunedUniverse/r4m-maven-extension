package net.runeduniverse.tools.runes4tools.maven.r4m.pem.view;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Goal;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.view.PhaseView;

public class Phase implements PhaseView {

	private String id;
	private List<Goal> goals = new LinkedList<>();

	public Phase(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public List<Goal> getGoals() {
		return this.goals;
	}

	@Override
	public void addGoal(Goal goal) {
		this.goals.add(goal);
	}

	@Override
	public void removeGoal(Goal goal) {
		this.goals.remove(goal);
	}

	@Override
	public void addAllGoals(Collection<Goal> goals) {
		this.goals.addAll(goals);
	}

}