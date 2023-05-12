package net.runeduniverse.tools.maven.r4m.pem.view.api;

import java.util.Collection;
import java.util.List;

import net.runeduniverse.lib.utils.logging.logs.Recordable;

public interface PhaseView extends Recordable {

	public String getId();

	public List<GoalView> getGoals();

	public void addGoal(GoalView goal);

	public void removeGoal(GoalView goal);

	public void addNewGoals(Collection<GoalView> goals);

}
