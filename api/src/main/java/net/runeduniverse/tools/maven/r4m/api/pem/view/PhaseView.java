package net.runeduniverse.tools.maven.r4m.api.pem.view;

import java.util.Collection;
import java.util.List;

import net.runeduniverse.tools.maven.r4m.api.pem.Recordable;

public interface PhaseView extends Recordable {
	public String getId();

	public List<GoalView> getGoals();

	public void addGoal(GoalView goal);

	public void removeGoal(GoalView goal);

	public void addAllGoals(Collection<GoalView> goals);
}
