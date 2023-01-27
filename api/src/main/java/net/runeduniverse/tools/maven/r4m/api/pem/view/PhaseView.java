package net.runeduniverse.tools.maven.r4m.api.pem.view;

import java.util.Collection;
import java.util.List;

import net.runeduniverse.tools.maven.r4m.api.pem.Recordable;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Goal;

public interface PhaseView extends Recordable {
	public String getId();

	public List<Goal> getGoals();

	public void addGoal(Goal goal);

	public void removeGoal(Goal goal);

	public void addAllGoals(Collection<Goal> goals);
}
