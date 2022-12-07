package net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.model;

import java.util.LinkedHashSet;
import java.util.Set;

//import net.runeduniverse.lib.utils.logging.logs.CompoundTree;

public class Phase {

	//private final CompoundTree record = new CompoundTree("Phase");
	private final String id;

	private final Set<Goal> goals;

	public Phase(final String id) {
		this.id = id;
		this.goals = new LinkedHashSet<>();
		//this.record.append("id", this.id);
	}

	public String getId() {
		return this.id;
	}

	public Set<Goal> getGoals() {
		return this.goals;
	}

	public void addGoal(Goal goal) {
		this.goals.add(goal);
	}

	//public void toRecord(CompoundTree record) {
	//	record.append(this.record);
	//}
}
