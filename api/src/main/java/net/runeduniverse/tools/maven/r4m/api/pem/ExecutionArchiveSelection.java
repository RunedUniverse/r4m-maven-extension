package net.runeduniverse.tools.maven.r4m.api.pem;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.runeduniverse.tools.maven.r4m.api.pem.view.ExecutionView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.GoalView;

public interface ExecutionArchiveSelection extends Recordable {

	public Map<ExecutionView, List<GoalView>> selectPhase(String phase);

	public Map<String, Map<ExecutionView, List<GoalView>>> selectPhases(String... phases);

	public Map<String, Map<ExecutionView, List<GoalView>>> selectPhases(final Collection<String> phases);

}
