package net.runeduniverse.tools.runes4tools.maven.r4m.pem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.ExecutionArchiveSlice;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.ExecutionSource;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Goal;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Lifecycle;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Phase;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.ProfileTrigger;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.view.ExecutionView;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.view.LifecycleView;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.view.PhaseView;
import net.runeduniverse.tools.runes4tools.maven.r4m.pem.view.ViewFactory;

public class Selection implements ExecutionArchiveSelection {

	private final Archive archive;

	private MavenProject mvnProject = null;
	private String packagingProcedure = null;
	private String activeExecution = null;
	private final Set<String> activeProfiles = new LinkedHashSet<>();
	private final Set<String> providedProfiles = new LinkedHashSet<>();
	private final Set<String> modes = new LinkedHashSet<>();

	public Selection(final Archive archive) {
		this.archive = archive;
	}

	public ExecutionArchiveSelection selectActiveProject(MavenProject value) {
		this.mvnProject = value;
		return this;
	}

	// used to select for the packaging flag in pom. Keep in mind there are multiple
	// packaging flags that set the type of how an artifact is packaged. So here we
	// define it as packaging procedure.
	public ExecutionArchiveSelection selectPackagingProcedure(String value) {
		this.packagingProcedure = value;
		return this;
	}

	public ExecutionArchiveSelection selectActiveExecution(String value) {
		this.activeExecution = value;
		return this;
	}

	public ExecutionArchiveSelection selectActiveProfiles(String... values) {
		for (int i = 0; i < values.length; i++)
			this.activeProfiles.add(values[i]);
		return this;
	}

	public ExecutionArchiveSelection selectActiveProfiles(Collection<String> values) {
		this.activeProfiles.addAll(values);
		return this;
	}

	public ExecutionArchiveSelection selectProvidedProfiles(String... values) {
		for (int i = 0; i < values.length; i++)
			this.providedProfiles.add(values[i]);
		return this;
	}

	public ExecutionArchiveSelection selectProvidedProfiles(Collection<String> values) {
		this.providedProfiles.addAll(values);
		return this;
	}

	public ExecutionArchiveSelection selectModes(String... values) {
		for (int i = 0; i < values.length; i++)
			this.modes.add(values[i]);
		return this;
	}

	public ExecutionArchiveSelection selectModes(Collection<String> values) {
		this.modes.addAll(values);
		return this;
	}

	public ExecutionArchiveSelection clearActiveProject() {
		this.mvnProject = null;
		return this;
	}

	public ExecutionArchiveSelection clearPackagingProcedure() {
		this.packagingProcedure = null;
		return this;
	}

	public ExecutionArchiveSelection clearActiveExecution() {
		this.activeExecution = null;
		return this;
	}

	public ExecutionArchiveSelection clearActiveProfiles() {
		this.activeProfiles.clear();
		return this;
	}

	public ExecutionArchiveSelection clearProvidedProfiles() {
		this.providedProfiles.clear();
		return this;
	}

	public ExecutionArchiveSelection clearModes() {
		this.modes.clear();
		return this;
	}

	public Archive getArchive() {
		return this.archive;
	}

	public MavenProject getActiveProject() {
		return this.mvnProject;
	}

	public String getPackagingProcedure() {
		return this.packagingProcedure;
	}

	public String getActiveExecution() {
		return this.activeExecution;
	}

	public Set<String> getActiveProfiles() {
		return Collections.unmodifiableSet(this.activeProfiles);
	}

	public Set<String> getProvidedProfiles() {
		return Collections.unmodifiableSet(this.providedProfiles);
	}

	public Set<String> getModes() {
		return Collections.unmodifiableSet(this.modes);
	}

	@Override
	public Object clone() {
		Selection selection = new Selection(archive);
		selection.selectActiveProject(this.mvnProject);
		selection.selectPackagingProcedure(this.packagingProcedure);
		selection.selectActiveProfiles(Collections.unmodifiableSet(this.activeProfiles));
		selection.selectProvidedProfiles(Collections.unmodifiableSet(this.providedProfiles));
		selection.selectModes(Collections.unmodifiableSet(this.modes));
		return selection;
	}

	public void copy(ExecutionArchiveSelection selection) {
		this.mvnProject = selection.getActiveProject();
		this.packagingProcedure = selection.getPackagingProcedure();
		selectActiveProfiles(selection.getActiveProfiles());
		selectProvidedProfiles(selection.getProvidedProfiles());
		selectModes(selection.getModes());
	}

	protected Set<Execution> filterSlice(ExecutionArchiveSlice slice) {
		return slice.getExecutions(execution -> {
			if (execution.isNeverActive())
				return false;
			if (execution.isAlwaysActive())
				return true;
			if (execution.getPackagingProcedures()
					.contains(this.packagingProcedure))
				return true;
			if (execution.getId()
					.equals(this.activeExecution))
				return true;
			for (ProfileTrigger trigger : execution.getTrigger())
				if (trigger.isActive(this.activeProfiles, this.providedProfiles))
					return true;
			return false;
		});
	}

	protected boolean validateGoal(Goal goal) {
		for (String mode : this.modes)
			if (goal.getModes()
					.contains(mode))
				return true;
		return false;
	}

	protected Map<String, Map<ExecutionSource, ExecutionView>> aggregate(Collection<Execution> executions) {
		Map<String, Map<ExecutionSource, ExecutionView>> map = new LinkedHashMap<>();

		for (Execution execution : executions) {
			Map<ExecutionSource, ExecutionView> entry = map.get(execution.getId());
			if (entry == null) {
				entry = new LinkedHashMap<>();
				map.put(execution.getId(), entry);
			}
			ExecutionView executionView = entry.get(execution.getSource());
			if (executionView == null) {
				executionView = ViewFactory.createExecution(execution.getId());
				entry.put(execution.getSource(), executionView);
			}
			for (Lifecycle lifecycle : execution.getLifecycles()
					.values()) {
				LifecycleView lifecycleView = executionView.getLifecycle(lifecycle.getId());
				if (lifecycleView == null) {
					lifecycleView = ViewFactory.createLifecycle(lifecycle.getId());
					executionView.put(lifecycleView);
				}
				for (Phase phase : lifecycle.getPhases()
						.values()) {
					PhaseView phaseView = lifecycleView.getPhase(phase.getId());
					if (phaseView == null) {
						phaseView = ViewFactory.createPhase(phase.getId());
						lifecycleView.put(phaseView);
					}
					for (Goal goal : phase.getGoals())
						if (this.validateGoal(goal))
							phaseView.addGoal(goal);
				}
			}
		}
		return map;
	}

	protected ExecutionView merge(final ExecutionView base, final ExecutionView dominant, boolean lifecycleOverride,
			boolean goalOverride) {
		for (LifecycleView domLifecycle : dominant.getLifecycles()
				.values()) {
			LifecycleView baseLifecycle = base.getLifecycles()
					.get(domLifecycle.getId());
			if (lifecycleOverride || baseLifecycle == null) {
				base.put(domLifecycle);
				continue;
			}

			// remove all dominant goals from base phases, which can only be executed once
			Map<Goal, PhaseView> basePhaseIndex = new HashMap<>();
			for (PhaseView basePhase : baseLifecycle.getPhases()
					.values())
				for (Goal goal : basePhase.getGoals())
					basePhaseIndex.put(goal, basePhase);

			for (PhaseView domPhase : domLifecycle.getPhases()
					.values())
				for (Goal domGoal : domPhase.getGoals())
					if (goalOverride || !domGoal.getDescriptor()
							.alwaysExecute())
						for (Goal baseGoal : basePhaseIndex.keySet()) {
							if (domGoal.equals(baseGoal))
								basePhaseIndex.get(baseGoal)
										.removeGoal(baseGoal);
						}

			// push all dominant phases and goals into base lifecycles
			for (PhaseView domPhase : domLifecycle.getPhases()
					.values()) {
				PhaseView basePhase = baseLifecycle.getPhases()
						.get(domPhase.getId());
				if (basePhase == null) {
					baseLifecycle.put(domPhase);
					continue;
				}
				basePhase.addAllGoals(domPhase.getGoals());
			}
		}
		return base;
	}

	@SuppressWarnings("deprecation")
	protected Map<String, Map<ExecutionSource, ExecutionView>> getExecutions(ExecutionArchiveSlice slice) {
		Map<String, Map<ExecutionSource, ExecutionView>> baseViews;
		if (slice.getParent() == null)
			baseViews = new LinkedHashMap<>();
		else
			baseViews = getExecutions(slice.getParent());

		Set<Execution> applyableExecutions = filterSlice(slice);
		Map<String, Map<ExecutionSource, ExecutionView>> dominantViews = aggregate(applyableExecutions);

		for (String executionId : dominantViews.keySet()) {
			Map<ExecutionSource, ExecutionView> baseEntries = baseViews.get(executionId);
			Map<ExecutionSource, ExecutionView> domEntries = dominantViews.get(executionId);
			if (baseEntries == null) {
				baseViews.put(executionId, domEntries);
				continue;
			}
			baseEntries.remove(ExecutionSource.EFFECTIVE);
			for (ExecutionSource source : domEntries.keySet()) {
				ExecutionView baseExecution = baseEntries.get(source);
				ExecutionView domExecution = domEntries.get(source);
				if (baseExecution == null || ExecutionSource.EFFECTIVE.equals(source)) {
					baseEntries.put(source, domExecution);
					continue;
				}
				baseEntries.put(source,
						merge(baseExecution, domExecution, ExecutionSource.OVERRIDE.equals(source), false));
			}
		}

		return baseViews;
	}

	@SuppressWarnings("deprecation")
	protected ExecutionView reduce(String id, Map<ExecutionSource, ExecutionView> views) {
		ExecutionView execution = views.get(ExecutionSource.EFFECTIVE);
		if (execution != null)
			return execution;
		ExecutionView pluginView = views.getOrDefault(ExecutionSource.PLUGIN, ViewFactory.createExecution(id));
		ExecutionView packagingView = views.getOrDefault(ExecutionSource.PACKAGING, ViewFactory.createExecution(id));
		ExecutionView overrideView = views.getOrDefault(ExecutionSource.OVERRIDE, ViewFactory.createExecution(id));

		execution = merge(pluginView, packagingView, false, true);
		execution = merge(execution, overrideView, true, false);

		return execution;
	}

	@Override
	public Set<ExecutionView> getEffectiveExecutions() {
		Set<ExecutionView> views = new LinkedHashSet<>();
		if (this.mvnProject == null)
			return views;

		ExecutionArchiveSlice slice = this.archive.getSlice(this.mvnProject);
		if (slice == null)
			return views;

		for (Entry<String, Map<ExecutionSource, ExecutionView>> entry : getExecutions(slice).entrySet())
			views.add(reduce(entry.getKey(), entry.getValue()));

		return views;
	}
}
