package net.runeduniverse.tools.maven.r4m.mojos;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Trigger;

/**
 * generates the full pem.xml from all active maven defaults
 * 
 * @author Pl4yingNight
 * @goal generate-full-pem
 */
public class GenerateFullPemMojo extends AbstractMojo {
	/**
	 * @parameter default-value="${session}"
	 * @readonly
	 */
	private MavenSession mvnSession;
	/**
	 * @parameter default-value="${project}"
	 * @readonly
	 */
	private MavenProject mvnProject;
	/**
	 * @component
	 */
	private ExecutionArchive archive;
	/**
	 * @component
	 */
	private ProjectExecutionModelWriter writer;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub
		getLog().info("building full pem.xml");

		ExecutionArchiveSlice projectSlice = this.archive.getSlice(this.mvnProject);

		Set<Execution> executions = new HashSet<>();
		collectExecutions(executions, projectSlice);

		Map<String, Map<ExecutionSource, List<Execution>>> map = new LinkedHashMap<>();
		reduce(map, executions);

		getLog().warn("reduced");
		for (Map<ExecutionSource, List<Execution>> x : map.values()) {
			for (List<Execution> y : x.values()) {
				for (Execution execution : y) {
					getLog().info(String.format("id: %s\tsource: %s\tpackaging: [%s]", execution.getId(),
							execution.getSource(), String.join(", ", execution.getPackagingProcedures())));
				}
			}
		}

		Map<String, Map<ExecutionSource, List<Execution>>> mergedMap = new LinkedHashMap<>();
		merge(map, mergedMap);

		getLog().warn("merged");
		for (Map<ExecutionSource, List<Execution>> x : mergedMap.values()) {
			for (List<Execution> y : x.values()) {
				for (Execution execution : y) {
					getLog().info(String.format("id: %s\tsource: %s\tpackaging: [%s]", execution.getId(),
							execution.getSource(), String.join(", ", execution.getPackagingProcedures())));
				}
			}
		}
	}

	private void merge(final Map<String, Map<ExecutionSource, List<Execution>>> map,
			final Map<String, Map<ExecutionSource, List<Execution>>> mergeMap) {
		for (Entry<String, Map<ExecutionSource, List<Execution>>> origSourceEntry : map.entrySet()) {
			Map<ExecutionSource, List<Execution>> mergeSource = mergeMap.get(origSourceEntry.getKey());
			if (mergeSource == null) {
				mergeSource = new LinkedHashMap<>();
				mergeMap.put(origSourceEntry.getKey(), mergeSource);
			}

			for (Entry<ExecutionSource, List<Execution>> origColEntry : origSourceEntry.getValue()
					.entrySet()) {
				List<Execution> mergeCol = mergeSource.get(origColEntry.getKey());
				if (mergeCol == null) {
					mergeCol = new LinkedList<>();
					mergeSource.put(origColEntry.getKey(), mergeCol);
				}

				List<Execution> execCol = new LinkedList<>(origColEntry.getValue());
				for (Iterator<Execution> i = origColEntry.getValue()
						.iterator(); i.hasNext();) {
					Execution origExec = i.next();
					// we don't merge yourself with yourself
					if (execCol.contains(origExec))
						execCol.remove(origExec);
					else
						// cant find yourself -> already merged
						continue;
					// check if special condition is active!
					boolean matchAnyPackagingProcedure = origExec.getPackagingProcedures()
							.isEmpty();
					mergeCol.add(origExec);

					for (Iterator<Execution> t = execCol.iterator(); t.hasNext();) {
						Execution exec = (Execution) t.next();

						// origExec similar to exec?
						if (!isSimilar(origExec, exec, false))
							continue;
						if (matchAnyPackagingProcedure)
							if (!exec.getPackagingProcedures()
									.isEmpty())
								continue;
						t.remove();
						origExec.getPackagingProcedures()
								.addAll(exec.getPackagingProcedures());

						for (Lifecycle lifecycle : exec.getLifecycles()
								.values()) {
							Lifecycle mergeLifecycle = origExec.getLifecycle(lifecycle.getId());
							if (mergeLifecycle == null) {
								origExec.putLifecycle(lifecycle);
								continue;
							}

							for (Phase phase : lifecycle.getPhases()
									.values()) {
								Phase mergePhase = mergeLifecycle.getPhase(phase.getId());
								if (mergePhase == null) {
									mergeLifecycle.putPhase(phase);
									continue;
								}

								for (Iterator<Goal> g = phase.getGoals()
										.iterator(); g.hasNext();) {
									Goal goal = (Goal) g.next();
									for (Goal mergeGoal : mergePhase.getGoals())
										if (isSimilar(mergeGoal, goal, false)) {
											mergeGoal.addModes(goal.getModes());
											g.remove();
										}
								}
								mergePhase.addGoals(phase.getGoals());
							}
						}
					}
				}
			}
		}
	}

	private void reduce(final Map<String, Map<ExecutionSource, List<Execution>>> map, final Set<Execution> executions) {
		for (Execution sExec : executions) {
			Map<ExecutionSource, List<Execution>> sourceMap = map.get(sExec.getId());
			if (sourceMap == null) {
				sourceMap = new LinkedHashMap<>();
				map.put(sExec.getId(), sourceMap);
			}
			List<Execution> execPool = sourceMap.get(sExec.getSource());
			if (execPool == null) {
				execPool = new LinkedList<>();
				sourceMap.put(sExec.getSource(), execPool);
			}
			Execution exec = getEquivalent(execPool, sExec);
			if (exec == null) {
				exec = createEquivalent(sExec);
				execPool.add(exec);
			}

			for (Lifecycle sLifecycle : sExec.getLifecycles()
					.values()) {
				Lifecycle lifecycle = exec.getLifecycle(sLifecycle.getId());
				if (lifecycle == null) {
					lifecycle = new Lifecycle(sLifecycle.getId());
					exec.putLifecycle(lifecycle);
				}

				for (Phase sPhase : sLifecycle.getPhases()
						.values()) {
					Phase phase = lifecycle.getPhase(sPhase.getId());
					if (phase == null) {
						phase = new Phase(sPhase.getId());
						lifecycle.putPhase(phase);
					}

					phase.addGoals(sPhase.getGoals());
				}
			}
		}
	}

	private boolean isSimilar(final Execution origExec, final Execution exec, final boolean checkPackagingProcedures) {
		// id
		final String id = origExec.getId();
		if (id == null) {
			if (exec.getId() != null)
				return false;
		} else if (!id.equals(exec.getId()))
			return false;
		// source
		final ExecutionSource source = origExec.getSource();
		if (source == null) {
			if (exec.getSource() != null)
				return false;
		} else if (!source.equals(exec.getSource()))
			return false;
		// active flags
		if (origExec.isAlwaysActive() != exec.isAlwaysActive() || origExec.isDefaultActive() != exec.isDefaultActive()
				|| origExec.isNeverActive() != exec.isNeverActive())
			return false;
		// packagingProcedures
		if (checkPackagingProcedures) {
			final Set<String> execPackagingProcedures = exec.getPackagingProcedures();
			if (execPackagingProcedures.size() != origExec.getPackagingProcedures()
					.size())
				return false;
			if (!execPackagingProcedures.containsAll(origExec.getPackagingProcedures()))
				return false;
		}
		// trigger
		final List<Trigger> execTrigger = new LinkedList<>(exec.getTrigger());
		if (execTrigger.size() != origExec.getTrigger()
				.size())
			return false;
		if (!execTrigger.containsAll(origExec.getTrigger()))
			return false;
		// equal metadata
		return true;
	}

	private boolean isSimilar(final Goal origGoal, final Goal goal, final boolean checkModes) {
		if (origGoal == goal)
			return true;

		if (origGoal.getGroupId() == null) {
			if (goal.getGroupId() != null)
				return false;
		} else if (!origGoal.getGroupId()
				.equals(goal.getGroupId()))
			return false;

		if (origGoal.getArtifactId() == null) {
			if (goal.getArtifactId() != null)
				return false;
		} else if (!origGoal.getArtifactId()
				.equals(goal.getArtifactId()))
			return false;

		if (origGoal.getGoalId() == null) {
			if (goal.getGoalId() != null)
				return false;
		} else if (!origGoal.getGoalId()
				.equals(goal.getGoalId()))
			return false;

		if (checkModes && !(origGoal.getModes()
				.size() == goal.getModes()
						.size()
				&& origGoal.getModes()
						.containsAll(goal.getModes())))
			return false;

		if (origGoal.getFork() != null)
			return origGoal.getFork()
					.equals(goal.getFork());

		return true;
	}

	private Execution getEquivalent(final List<Execution> pool, final Execution original) {
		for (Execution exec : pool)
			if (isSimilar(original, exec, true))
				return exec;
		return null;
	}

	private Execution createEquivalent(final Execution original) {
		Execution equivalent = new Execution(original.getId(), original.getSource());
		equivalent.setAlwaysActive(original.isAlwaysActive());
		equivalent.setDefaultActive(original.isDefaultActive());
		equivalent.setNeverActive(original.isNeverActive());
		equivalent.getPackagingProcedures()
				.addAll(original.getPackagingProcedures());
		equivalent.getTrigger()
				.addAll(original.getTrigger());
		return equivalent;
	}

	private void collectExecutions(Set<Execution> executions, final ExecutionArchiveSlice slice) {
		if (slice == null)
			return;
		executions.addAll(slice.getExecutions());
		collectExecutions(executions, slice.getParent());
	}
}
