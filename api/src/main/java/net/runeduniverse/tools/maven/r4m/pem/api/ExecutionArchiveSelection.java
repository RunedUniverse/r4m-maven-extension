package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.runeduniverse.tools.maven.r4m.pem.view.api.ExecutionView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.GoalView;

public interface ExecutionArchiveSelection extends Recordable {

	public void modify(Modification mod);

	public Map<ExecutionView, List<GoalView>> selectPhase(String phase);

	public Map<String, Map<ExecutionView, List<GoalView>>> selectPhases(String... phases);

	public Map<String, Map<ExecutionView, List<GoalView>>> selectPhases(final Collection<String> phases);

	public ExecutionArchiveSelectorConfig getSelectorConfig();

	@FunctionalInterface
	public interface Modification {

		public void modify(final Set<ExecutionView> views);

	}

}
