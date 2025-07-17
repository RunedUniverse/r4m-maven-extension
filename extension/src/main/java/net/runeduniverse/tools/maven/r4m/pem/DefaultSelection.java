/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import net.runeduniverse.lib.utils.common.api.DataMap;
import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.pem.view.api.ExecutionView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.GoalView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.LifecycleView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.PhaseView;

public class DefaultSelection implements ExecutionArchiveSelection {

	protected final ExecutionArchiveSelectorConfig selectorConfig;
	protected final DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> overrides;
	protected final Map<String, String> overrideModelReference;
	protected final Set<ExecutionView> views;

	public DefaultSelection(final ExecutionArchiveSelectorConfig selectorConfig,
			final DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> overrides,
			final Map<String, String> overrideModelReference, final Set<ExecutionView> views) {
		this.selectorConfig = selectorConfig;
		this.overrides = overrides;
		this.overrideModelReference = overrideModelReference;
		this.views = views;
	}

	@Override
	public void modify(final Consumer<Set<ExecutionView>> mod) {
		mod.accept(this.views);
		cleanup();
	}

	protected void cleanup() {
		for (Iterator<ExecutionView> iExec = views.iterator(); iExec.hasNext();) {
			final ExecutionView exec = iExec.next();

			for (Iterator<LifecycleView> iLifecycle = exec.getLifecycles()
					.values()
					.iterator(); iLifecycle.hasNext();) {
				final LifecycleView lifecycle = iLifecycle.next();

				for (Iterator<PhaseView> iPhase = lifecycle.getPhases()
						.values()
						.iterator(); iPhase.hasNext();) {
					final PhaseView phase = iPhase.next();
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
	public Map<ExecutionView, List<GoalView>> selectPhase(final String selected) {
		final Map<ExecutionView, List<GoalView>> goalMap = new LinkedHashMap<>();
		if (selected == null)
			return goalMap;

		for (ExecutionView exec : this.views) {
			for (LifecycleView lifecycle : exec.getLifecycles()
					.values())
				for (PhaseView phase : lifecycle.getPhases()
						.values())
					if (selected.equals(phase.getId()))
						for (GoalView goal : phase.getGoals()) {
							final List<GoalView> goals = goalMap.computeIfAbsent(exec, k -> new LinkedList<>());
							goals.add(goal);
						}
		}
		return goalMap;
	}

	@Override
	public Map<String, Map<ExecutionView, List<GoalView>>> selectPhases(final String... phases) {
		return selectPhases(Arrays.asList(phases));
	}

	@Override
	public Map<String, Map<ExecutionView, List<GoalView>>> selectPhases(final Collection<String> phases) {
		final Map<String, Map<ExecutionView, List<GoalView>>> map = new LinkedHashMap<>();
		for (ExecutionView exec : this.views) {
			for (LifecycleView lifecycle : exec.getLifecycles()
					.values())
				for (PhaseView phase : lifecycle.getPhases()
						.values()) {
					final String phaseId = phase.getId();
					for (GoalView goal : phase.getGoals())
						if (phases.contains(phase.getId())) {
							final Map<ExecutionView, List<GoalView>> goalMap = map.computeIfAbsent(phaseId,
									k -> new LinkedHashMap<>());
							final List<GoalView> goals = goalMap.computeIfAbsent(exec, k -> new LinkedList<>());
							goals.add(goal);
						}
				}
		}
		return map;
	}

	@Override
	public ExecutionArchiveSelectorConfig getSelectorConfig() {
		return this.selectorConfig;
	}

	@Override
	public DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> getOverrides() {
		return this.overrides;
	}

	@Override
	public Map<String, String> getOverrideModelReference() {
		return this.overrideModelReference;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("PEM Selection");

		for (ExecutionView view : views)
			tree.append(view.toRecord());

		return tree;
	}

	@Override
	public String toString() {
		return toRecord().toString();
	}

}
