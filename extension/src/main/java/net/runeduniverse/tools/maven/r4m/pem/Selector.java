package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelector;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Trigger;
import net.runeduniverse.tools.maven.r4m.api.pem.view.ExecutionView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.LifecycleView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.PhaseView;
import net.runeduniverse.tools.maven.r4m.pem.view.ViewFactory;

public class Selector implements ExecutionArchiveSelector {

	private final MavenSession mvnSession;
	private final ExecutionArchive archive;

	private SelectorConfig cnf = null;

	public Selector(final MavenSession mvnSession, final ExecutionArchive archive) {
		this(mvnSession, archive, new SelectorConfig());
	}

	public Selector(final MavenSession mvnSession, final ExecutionArchive archive, SelectorConfig cnf) {
		this.mvnSession = mvnSession;
		this.archive = archive;
		this.cnf = cnf;
	}

	public ExecutionArchiveSelector selectActiveProject(MavenProject value) {
		this.cnf.selectActiveProject(value);
		return this;
	}

	// used to select for the packaging flag in pom. Keep in mind there are multiple
	// packaging flags that set the type of how an artifact is packaged. So here we
	// define it as packaging procedure.
	public ExecutionArchiveSelector selectPackagingProcedure(String value) {
		this.cnf.selectPackagingProcedure(value);
		return this;
	}

	public ExecutionArchiveSelector selectActiveExecution(String value) {
		this.cnf.selectActiveExecution(value);
		return this;
	}

	public ExecutionArchiveSelector selectActiveProfiles(String... values) {
		this.cnf.selectActiveProfiles(values);
		return this;
	}

	public ExecutionArchiveSelector selectActiveProfiles(Collection<String> values) {
		this.cnf.selectActiveProfiles(values);
		return this;
	}

	public ExecutionArchiveSelector selectProvidedProfiles(String... values) {
		this.cnf.selectProvidedProfiles(values);
		return this;
	}

	public ExecutionArchiveSelector selectProvidedProfiles(Collection<String> values) {
		this.selectProvidedProfiles(values);
		return this;
	}

	public ExecutionArchiveSelector selectModes(String... values) {
		this.cnf.selectModes(values);
		return this;
	}

	public ExecutionArchiveSelector selectModes(Collection<String> values) {
		this.cnf.selectModes(values);
		return this;
	}

	public ExecutionArchiveSelector clearActiveProject() {
		this.cnf.clearActiveProject();
		return this;
	}

	public ExecutionArchiveSelector clearPackagingProcedure() {
		this.cnf.clearPackagingProcedure();
		return this;
	}

	public ExecutionArchiveSelector clearActiveExecution() {
		this.cnf.clearActiveExecution();
		return this;
	}

	public ExecutionArchiveSelector clearActiveProfiles() {
		this.cnf.clearActiveProfiles();
		return this;
	}

	public ExecutionArchiveSelector clearProvidedProfiles() {
		this.cnf.clearProvidedProfiles();
		return this;
	}

	public ExecutionArchiveSelector clearModes() {
		this.cnf.clearModes();
		return this;
	}

	public ExecutionArchive getArchive() {
		return this.archive;
	}

	@Override
	public MavenSession getMvnSession() {
		return this.mvnSession;
	}

	public MavenProject getActiveProject() {
		return this.cnf.getActiveProject();
	}

	public String getPackagingProcedure() {
		return this.cnf.getPackagingProcedure();
	}

	public String getActiveExecution() {
		return this.cnf.getActiveExecution();
	}

	public Set<String> getActiveProfiles() {
		return this.cnf.getActiveProfiles();
	}

	public Set<String> getProvidedProfiles() {
		return this.cnf.getProvidedProfiles();
	}

	public Set<String> getModes() {
		return this.cnf.getModes();
	}

	@Override
	public Object clone() {
		return new Selector(this.mvnSession, this.archive, this.cnf.clone());
	}

	protected Set<Execution> filterSlice(ExecutionArchiveSlice slice) {
		return slice.getExecutions(execution -> {
			if (execution.isNeverActive())
				return false;
			if (execution.isAlwaysActive())
				return true;
			if (execution.getPackagingProcedures()
					.contains(this.cnf.getPackagingProcedure()))
				return true;
			if (this.cnf.getActiveExecution() == null) {
				if (execution.isDefaultActive())
					return true;
			} else if (execution.getId()
					.equals(this.cnf.getActiveExecution()))
				return true;
			for (Trigger trigger : execution.getTrigger())
				if (trigger.isActive(this.cnf))
					return true;
			return false;
		});
	}

	protected boolean validateGoal(Goal goal) {
		for (String mode : this.cnf.getModes())
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
	public ExecutionArchiveSelection compile() {
		Set<ExecutionView> views = new LinkedHashSet<>();
		if (this.cnf.getActiveProject() == null)
			return new Selection(views);

		ExecutionArchiveSlice slice = this.archive.getSlice(this.cnf.getActiveProject());
		if (slice == null)
			return new Selection(views);

		this.cnf.compile(this.mvnSession);
		for (Entry<String, Map<ExecutionSource, ExecutionView>> entry : getExecutions(slice).entrySet())
			views.add(reduce(entry.getKey(), entry.getValue()));
		return new Selection(views);
	}
}
