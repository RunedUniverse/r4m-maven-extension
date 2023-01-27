package net.runeduniverse.tools.maven.r4m.api.pem.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Phase {
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
}