package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.api.pem.view.ExecutionView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.GoalView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.LifecycleView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.PhaseView;

public class Selection implements ExecutionArchiveSelection {

	protected final Set<ExecutionView> views;

	public Selection(Set<ExecutionView> views) {
		this.views = views;
	}

	@Override
	public Map<ExecutionView, List<GoalView>> selectPhase(String selected) {
		Map<ExecutionView, List<GoalView>> goalMap = new LinkedHashMap<>();
		if (selected == null)
			return goalMap;

		for (ExecutionView exec : this.views)
			for (LifecycleView lifecycle : exec.getLifecycles()
					.values())
				for (PhaseView phase : lifecycle.getPhases()
						.values())
					for (GoalView goal : phase.getGoals())
						if (selected.equals(phase.getId())) {
							List<GoalView> goals = goalMap.get(exec);
							if (goals == null) {
								goals = new LinkedList<>();
								goalMap.put(exec, goals);
							}
							goals.add(goal);
						}
		return goalMap;
	}

	@Override
	public Map<String, Map<ExecutionView, List<GoalView>>> selectPhases(String... phases) {
		return selectPhases(Arrays.asList(phases));
	}

	@Override
	public Map<String, Map<ExecutionView, List<GoalView>>> selectPhases(Collection<String> phases) {
		Map<String, Map<ExecutionView, List<GoalView>>> map = new LinkedHashMap<>();
		for (ExecutionView exec : this.views)
			for (LifecycleView lifecycle : exec.getLifecycles()
					.values())
				for (PhaseView phase : lifecycle.getPhases()
						.values()) {
					Map<ExecutionView, List<GoalView>> goalMap = map.get(phase.getId());
					for (GoalView goal : phase.getGoals())
						if (phases.contains(phase.getId())) {
							if (goalMap == null) {
								goalMap = new LinkedHashMap<>();
								map.put(phase.getId(), goalMap);
							}
							List<GoalView> goals = goalMap.get(exec);
							if (goals == null) {
								goals = new LinkedList<>();
								goalMap.put(exec, goals);
							}
							goals.add(goal);
						}
				}
		return map;
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("Selection");

		for (ExecutionView view : views)
			tree.append(view.toRecord());

		return tree;
	}

	@Override
	public String toString() {
		return toRecord().toString();
	}

}
