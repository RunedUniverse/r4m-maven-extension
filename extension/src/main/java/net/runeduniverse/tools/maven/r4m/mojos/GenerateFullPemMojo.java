package net.runeduniverse.tools.maven.r4m.mojos;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionTrigger;

import static net.runeduniverse.tools.maven.r4m.mojos.ExtensionUtils.acquireExecutionArchive;
import static net.runeduniverse.tools.maven.r4m.mojos.ExtensionUtils.mojoFailureExtensionLoading;

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

		ExecutionArchiveSlice projectSlice = null;
		if (this.archive != null)
			projectSlice = this.archive.getSlice(this.mvnProject);

		if (projectSlice == null) {
			// try loading via build-extension classrealm
			this.archive = acquireExecutionArchive(mvnSession, (ClassRealm) Thread.currentThread()
					.getContextClassLoader());
		}
		if (this.archive != null)
			projectSlice = this.archive.getSlice(this.mvnProject);

		if (projectSlice == null)
			mojoFailureExtensionLoading(getLog());

		Set<Execution> executions = new LinkedHashSet<>();
		int sliceCnt = collectExecutions(executions, projectSlice);

		getLog().info("");
		getLog().info("Discovery");
		getLog().info(String.format("    project depth:      %s", sliceCnt));
		getLog().info(String.format("    executions:         %s", executions.size()));
		getLog().info("    ------------------------");

		reduce(executions);

		getLog().info(String.format("    reduced executions: %s", executions.size()));
		getLog().info("");

		ProjectExecutionModel model = new ProjectExecutionModel();
		model.setVersion(Properties.PROJECT_EXECUTION_MODEL_VERSION);
		model.addExecutions(executions);

		OutputStream stream = this.writer.writeModel(model);
	}

	private void reduce(final Set<Execution> executions) {
		final Set<Execution> mergeCol = new LinkedHashSet<>();
		final Set<Execution> execSet = new LinkedHashSet<>(executions);
		final Set<Execution> remSet = new LinkedHashSet<>();
		for (Execution origExec : executions) {
			// don't merge it with itself
			if (execSet.contains(origExec))
				execSet.remove(origExec);
			else
				// cant find it -> already merged
				continue;
			// check if special condition is active!
			boolean matchRestrictions = origExec.getRestrictions()
					.isEmpty();
			// clone! originals must not be modified!!!
			Execution mergeExec = createEquivalent(origExec);
			mergeExec = reduce(origExec, null, false);
			mergeCol.add(mergeExec);

			for (Iterator<Execution> t = execSet.iterator(); t.hasNext();) {
				Execution exec = (Execution) t.next();

				if (!isSimilar(mergeExec, exec, false))
					continue;
				if (matchRestrictions)
					if (!exec.getRestrictions()
							.isEmpty())
						continue;

				Execution rem = reduce(exec, mergeExec, false);
				if (rem != null)
					remSet.add(rem);
				t.remove();
			}
		}

		execSet.clear();
		execSet.addAll(remSet);
		for (Execution remExec : remSet) {
			// don't merge it with itself
			if (execSet.contains(remExec))
				execSet.remove(remExec);
			else
				// cant find it -> already merged
				continue;
			mergeCol.add(remExec);

			for (Iterator<Execution> t = execSet.iterator(); t.hasNext();) {
				Execution exec = (Execution) t.next();

				if (!isSimilar(remExec, exec, true))
					continue;

				reduce(exec, remExec, true);
				t.remove();
			}
		}

		executions.clear();
		executions.addAll(mergeCol);
	}

	private Execution reduce(final Execution exec, final Execution mergeExec, boolean force) {
		if (mergeExec == null)
			force = false;
		Execution remExecution = createEquivalent(exec);

		boolean reduction = false;

		for (Lifecycle lifecycle : exec.getLifecycles()
				.values()) {
			Lifecycle mergeLifecycle = mergeExec == null ? null : mergeExec.getLifecycle(lifecycle.getId());
			Lifecycle remLifecycle = new Lifecycle(lifecycle.getId());
			if (mergeLifecycle == null) {
				mergeLifecycle = new Lifecycle(lifecycle.getId());
				if (force)
					mergeExec.putLifecycle(mergeLifecycle);
			}

			for (Phase phase : lifecycle.getPhases()
					.values()) {
				Phase mergePhase = mergeLifecycle.getPhase(phase.getId());
				Phase remPhase = new Phase(phase.getId());
				if (mergePhase == null) {
					mergePhase = new Phase(phase.getId());
					if (force)
						mergeLifecycle.putPhase(mergePhase);
				}

				for (Goal goal : phase.getGoals()) {
					boolean missing = true;
					for (Goal mergeGoal : mergePhase.getGoals())
						if (isSimilar(goal, mergeGoal, false)) {
							mergeGoal.addModes(goal.getModes());
							missing = false;
							reduction = true;
							break;
						}
					if (missing) {
						Goal equivalent = createEquivalent(goal);
						remPhase.addGoal(equivalent);
						if (force)
							mergePhase.addGoal(equivalent);
					}
				}
				if (!remPhase.getGoals()
						.isEmpty())
					remLifecycle.putPhase(remPhase);
			}
			if (!remLifecycle.getPhases()
					.isEmpty())
				remExecution.putLifecycle(remLifecycle);
		}

		if (reduction)
			mergeExec.getRestrictions()
					.addAll(exec.getRestrictions());

		if (remExecution.getLifecycles()
				.isEmpty())
			return null;
		return remExecution;
	}

	private boolean isSimilar(final Execution origExec, final Execution exec, final boolean checkRestrictions) {
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
		// inherited
		if (origExec.isInherited() != exec.isInherited())
			return false;
		// active flags
		if (origExec.isAlwaysActive() != exec.isAlwaysActive() || origExec.isDefaultActive() != exec.isDefaultActive()
				|| origExec.isNeverActive() != exec.isNeverActive())
			return false;
		// restrictions
		if (checkRestrictions) {
			final List<ExecutionRestriction> execRestrictions = new LinkedList<>(exec.getRestrictions());
			if (execRestrictions.size() != origExec.getRestrictions()
					.size())
				return false;
			if (!execRestrictions.containsAll(origExec.getRestrictions()))
				return false;
		}
		// trigger
		final List<ExecutionTrigger> execTrigger = new LinkedList<>(exec.getTrigger());
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

	private Execution createEquivalent(final Execution original) {
		Execution equivalent = new Execution(original.getId(), original.getSource());
		equivalent.setInherited(original.isInherited());
		equivalent.setAlwaysActive(original.isAlwaysActive());
		equivalent.setDefaultActive(original.isDefaultActive());
		equivalent.setNeverActive(original.isNeverActive());
		equivalent.getRestrictions()
				.addAll(original.getRestrictions());
		equivalent.getTrigger()
				.addAll(original.getTrigger());
		return equivalent;
	}

	private Goal createEquivalent(final Goal original) {
		Goal equivalent = new Goal(original.getGroupId(), original.getArtifactId(), original.getGoalId());
		equivalent.addModes(original.getModes());
		equivalent.setFork(original.getFork());
		return equivalent;
	}

	private int collectExecutions(final Set<Execution> executions, final ExecutionArchiveSlice slice) {
		return collectExecutions(executions, slice, false);
	}

	private int collectExecutions(final Set<Execution> executions, final ExecutionArchiveSlice slice,
			final boolean onlyInherited) {
		if (slice == null)
			return 0;
		executions.addAll(slice.getExecutions(e -> true, onlyInherited));
		return collectExecutions(executions, slice.getParent(), true) + 1;
	}
}
