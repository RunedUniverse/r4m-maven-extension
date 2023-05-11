package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.api.pem.view.ExecutionView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.GoalView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.LifecycleView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.PhaseView;

public class Selection implements ExecutionArchiveSelection {

	protected final ExecutionArchiveSelectorConfig selectorConfig;
	protected final Set<ExecutionView> views;

	public Selection(final ExecutionArchiveSelectorConfig selectorConfig, final Set<ExecutionView> views) {
		this.selectorConfig = selectorConfig;
		this.views = views;
	}

	@Override
	public void modify(Modification mod) {
		mod.modify(this.views);
		cleanup();
	}

	protected void cleanup() {
		for (Iterator<ExecutionView> iExec = views.iterator(); iExec.hasNext();) {
			ExecutionView exec = iExec.next();

			for (Iterator<LifecycleView> iLifecycle = exec.getLifecycles()
					.values()
					.iterator(); iLifecycle.hasNext();) {
				LifecycleView lifecycle = iLifecycle.next();

				for (Iterator<PhaseView> iPhase = lifecycle.getPhases()
						.values()
						.iterator(); iPhase.hasNext();) {
					PhaseView phase = iPhase.next();
					if (phase.getGoals()
							.isEmpty())
						iPhase.remove();
				}
				if (lifecycle.getPhases()
						.isEmpty())
					iLifecycle.remove();
			}
			if (exec.getLifecycles()
					.isEmpty())
				iExec.remove();
		}
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
					if (selected.equals(phase.getId()))
						for (GoalView goal : phase.getGoals()) {
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
	public ExecutionArchiveSelectorConfig getSelectorConfig() {
		return this.selectorConfig;
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
