package net.runeduniverse.tools.maven.r4m.mojos;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
			this.archive = acquireExecutionArchive(mvnSession,
					(ClassRealm) Thread.currentThread().getContextClassLoader());
		}
		if (this.archive != null)
			projectSlice = this.archive.getSlice(this.mvnProject);

		if (projectSlice == null)
			mojoFailureExtensionLoading(getLog());

		Set<Execution> executions = new LinkedHashSet<>();
		int sliceCnt = collectExecutions(executions, projectSlice);
		// clone! originals must not be modified!!!
		replaceWithEquivalents(executions);

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

		getLog().debug(model.toRecord().toString());

		OutputStream stream = this.writer.writeModel(model);
	}

	private void reduce(final Set<Execution> executions) {
		if (executions.size() < 2)
			return;
		final Set<Execution> mergeCol = new LinkedHashSet<>();
		final List<Execution> execCol = new LinkedList<>(executions);
		final Set<Execution> remCol = new LinkedHashSet<>();
		final List<Execution> origCol = new LinkedList<>(executions);
		for (ListIterator<Execution> iOrig = origCol.listIterator(); iOrig.hasNext();) {
			Execution origExec = (Execution) iOrig.next();
			// don't merge it with itself
			if (execCol.contains(origExec))
				execCol.remove(origExec);
			else
				// cant find it -> already merged
				continue;
			// check if special condition is active!
			boolean matchRestrictions = origExec.getRestrictions().isEmpty();

			for (ListIterator<Execution> iExec = execCol.listIterator(); iExec.hasNext();) {
				Execution exec = (Execution) iExec.next();

				if (!isSimilar(origExec, exec, false))
					continue;
				if (matchRestrictions)
					if (!exec.getRestrictions().isEmpty())
						continue;

				Execution reduced = reduce(origExec, exec, false);
				if (!origExec.getLifecycles().isEmpty())
					remCol.add(origExec);
				if (!reduced.getLifecycles().isEmpty())
					origExec = reduced;
				if (!exec.getLifecycles().isEmpty())
					remCol.add(exec);
				iExec.remove();
			}
		}
		/*
		 * execSet.clear(); execSet.addAll(remSet); for (Execution remExec : remSet) {
		 * // don't merge it with itself if (execSet.contains(remExec))
		 * execSet.remove(remExec); else // cant find it -> already merged continue;
		 * 
		 * for (Iterator<Execution> t = execSet.iterator(); t.hasNext();) { Execution
		 * exec = (Execution) t.next();
		 * 
		 * if (!isSimilar(remExec, exec, true)) continue;
		 * 
		 * Execution reduced = reduce(remExec, exec, true); if (reduced != null)
		 * mergeCol.add(reduced); t.remove(); } }
		 */

		executions.clear();
		executions.addAll(mergeCol);
	}

	private Execution reduce(final Execution domExec, final Execution secExec, boolean force) {
		Execution mergeExecution = createEquivalent(domExec);
		// force contains via equals
		final List<ExecutionRestriction> restrictions = new LinkedList<>(secExec.getRestrictions());
		restrictions.removeAll(mergeExecution.getRestrictions());
		mergeExecution.getRestrictions().addAll(restrictions);

		for (Iterator<Lifecycle> iDomLifecycle = domExec.getLifecycles().values().iterator(); iDomLifecycle
				.hasNext();) {
			Lifecycle domLifecycle = (Lifecycle) iDomLifecycle.next();
			Lifecycle secLifecycle = secExec.getLifecycle(domLifecycle.getId());
			if (secLifecycle == null)
				if (force)
					secLifecycle = new Lifecycle(domLifecycle.getId());
				else
					continue;
			Lifecycle mergeLifecycle = mergeExecution.getLifecycle(domLifecycle.getId());
			if (mergeLifecycle == null)
				mergeLifecycle = new Lifecycle(domLifecycle.getId());

			for (Iterator<Phase> iDomPhase = domLifecycle.getPhases().values().iterator(); iDomPhase.hasNext();) {
				Phase domPhase = (Phase) iDomPhase.next();
				Phase secPhase = secLifecycle.getPhase(domPhase.getId());
				if (secPhase == null)
					if (force)
						secPhase = new Phase(domPhase.getId());
					else
						continue;
				Phase mergePhase = mergeLifecycle.getPhase(domPhase.getId());
				if (mergePhase == null)
					mergePhase = new Phase(domPhase.getId());

				for (Iterator<Goal> iDomGoal = domPhase.getGoals().iterator(); iDomGoal.hasNext();) {
					Goal domGoal = (Goal) iDomGoal.next();
					for (Iterator<Goal> iSecGoal = secPhase.getGoals().iterator(); iSecGoal.hasNext();) {
						Goal secGoal = (Goal) iSecGoal.next();
						if (isSimilar(domGoal, secGoal, false)) {
							Goal mergeGoal = createEquivalent(domGoal);
							mergeGoal.addModes(secGoal.getModes());
							mergePhase.addGoal(mergeGoal);
							iSecGoal.remove();
							iDomGoal.remove();
						}
					}
					if (force) {
						mergePhase.addGoals(secPhase.getGoals());
						mergePhase.addGoals(domPhase.getGoals());
					}
				}
				if (secPhase.getGoals().isEmpty())
					secLifecycle.getPhases().remove(secPhase.getId());
				if (domPhase.getGoals().isEmpty())
					iDomPhase.remove();
				if (!mergePhase.getGoals().isEmpty())
					mergeLifecycle.putPhase(mergePhase);
			}
			if (secLifecycle.getPhases().isEmpty())
				secExec.getLifecycles().remove(secLifecycle.getId());
			if (domLifecycle.getPhases().isEmpty())
				iDomLifecycle.remove();
			if (!mergeLifecycle.getPhases().isEmpty())
				mergeExecution.putLifecycle(mergeLifecycle);
		}
		return mergeExecution;
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
			if (execRestrictions.size() != origExec.getRestrictions().size())
				return false;
			if (!execRestrictions.containsAll(origExec.getRestrictions()))
				return false;
		}
		// trigger
		final List<ExecutionTrigger> execTrigger = new LinkedList<>(exec.getTrigger());
		if (execTrigger.size() != origExec.getTrigger().size())
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
		} else if (!origGoal.getGroupId().equals(goal.getGroupId()))
			return false;

		if (origGoal.getArtifactId() == null) {
			if (goal.getArtifactId() != null)
				return false;
		} else if (!origGoal.getArtifactId().equals(goal.getArtifactId()))
			return false;

		if (origGoal.getGoalId() == null) {
			if (goal.getGoalId() != null)
				return false;
		} else if (!origGoal.getGoalId().equals(goal.getGoalId()))
			return false;

		if (checkModes && !(origGoal.getModes().size() == goal.getModes().size()
				&& origGoal.getModes().containsAll(goal.getModes())))
			return false;

		if (origGoal.getFork() != null)
			return origGoal.getFork().equals(goal.getFork());

		return true;
	}

	private void replaceWithEquivalents(final Set<Execution> executions) {
		final Set<Execution> equivalents = new LinkedHashSet<>();
		for (Execution exec : executions) {
			Execution equivalentExec = createEquivalent(exec);
			for (Lifecycle lifecycle : exec.getLifecycles().values()) {
				Lifecycle equivalentLifecycle = new Lifecycle(lifecycle.getId());
				equivalentExec.putLifecycle(equivalentLifecycle);
				for (Phase phase : lifecycle.getPhases().values()) {
					Phase equivalentPhase = new Phase(phase.getId());
					equivalentLifecycle.putPhase(equivalentPhase);
					equivalentPhase.addGoals(phase.getGoals());
				}
			}
			equivalents.add(equivalentExec);
		}
		executions.clear();
		executions.addAll(equivalents);
	}

	private Execution createEquivalent(final Execution original) {
		Execution equivalent = new Execution(original.getId(), original.getSource());
		equivalent.setInherited(original.isInherited());
		equivalent.setAlwaysActive(original.isAlwaysActive());
		equivalent.setDefaultActive(original.isDefaultActive());
		equivalent.setNeverActive(original.isNeverActive());
		equivalent.getRestrictions().addAll(original.getRestrictions());
		equivalent.getTrigger().addAll(original.getTrigger());
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
