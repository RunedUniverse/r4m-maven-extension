package net.runeduniverse.tools.maven.r4m.pem.view;

import net.runeduniverse.tools.maven.r4m.pem.view.api.ExecutionView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.GoalView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.LifecycleView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.PhaseView;

public class ViewFactory {

	public static ExecutionView createExecution(String id) {
		return new Execution(id);
	}

	public static LifecycleView createLifecycle(String id) {
		return new Lifecycle(id);
	}

	public static PhaseView createPhase(String id) {
		return new Phase(id);
	}

	public static GoalView createGoal(String groupId, String artifactId, String goalId) {
		return new Goal(groupId, artifactId, goalId);
	}

}
