package net.runeduniverse.tools.maven.r4m.mojos;

import java.util.HashSet;
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

import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;
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

		Set<Execution> reducedCol = new HashSet<>();
		reduce(executions, reducedCol);

		getLog().warn("reduced");
		for (Execution execution : reducedCol) {
			getLog().info(String.format("id: %s\tsource: %s\tpackaging: [%s]", execution.getId(), execution.getSource(),
					String.join(", ", execution.getPackagingProcedures())));
		}

		getLog().error("original:\t" + executions.size());
		getLog().error("reduced: \t" + reducedCol.size());

		ProjectExecutionModel model = new ProjectExecutionModel();
		model.setVersion(Properties.PROJECT_EXECUTION_MODEL_VERSION);
		model.addExecutions(reducedCol);

		getLog().warn(model.toRecord()
				.toString());
	}

	private void reduce(final Set<Execution> origCol, final Set<Execution> mergeCol) {
		Set<Execution> execSet = new LinkedHashSet<>(origCol);
		for (Execution origExec : origCol) {
			// we don't merge yourself with yourself
			if (execSet.contains(origExec))
				execSet.remove(origExec);
			else
				// cant find yourself -> already merged
				continue;
			// check if special condition is active!
			boolean matchAnyPackagingProcedure = origExec.getPackagingProcedures()
					.isEmpty();
			// clone! originals must not be modified!!!
			Execution mergeExec = createEquivalent(origExec);
			reduce(origExec, mergeExec);
			mergeCol.add(mergeExec);

			for (Iterator<Execution> t = execSet.iterator(); t.hasNext();) {
				Execution exec = (Execution) t.next();

				if (!isSimilar(mergeExec, exec, false))
					continue;
				if (matchAnyPackagingProcedure)
					if (!exec.getPackagingProcedures()
							.isEmpty())
						continue;
				mergeExec.getPackagingProcedures()
						.addAll(exec.getPackagingProcedures());
				reduce(exec, mergeExec);
				t.remove();
			}
		}
	}

	private void reduce(final Execution exec, final Execution mergeExec) {
		for (Lifecycle lifecycle : exec.getLifecycles()
				.values()) {
			Lifecycle mergeLifecycle = mergeExec.getLifecycle(lifecycle.getId());
			if (mergeLifecycle == null) {
				mergeLifecycle = new Lifecycle(lifecycle.getId());
				mergeExec.putLifecycle(mergeLifecycle);
			}

			for (Phase phase : lifecycle.getPhases()
					.values()) {
				Phase mergePhase = mergeLifecycle.getPhase(phase.getId());
				if (mergePhase == null) {
					mergePhase = new Phase(phase.getId());
					mergeLifecycle.putPhase(mergePhase);
				}

				List<Goal> mergeGoals = mergePhase.getGoals();
				for (Goal goal : phase.getGoals()) {
					boolean missing = true;
					for (Goal mergeGoal : mergeGoals)
						if (isSimilar(goal, mergeGoal, false)) {
							mergeGoal.addModes(goal.getModes());
							missing = false;
							break;
						}
					if (missing)
						mergeGoals.add(createEquivalent(goal));
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

	private Goal createEquivalent(final Goal original) {
		Goal equivalent = new Goal(original.getGroupId(), original.getArtifactId(), original.getGoalId());
		equivalent.addModes(original.getModes());
		equivalent.setFork(original.getFork());
		return equivalent;
	}

	private void collectExecutions(Set<Execution> executions, final ExecutionArchiveSlice slice) {
		if (slice == null)
			return;
		executions.addAll(slice.getExecutions());
		collectExecutions(executions, slice.getParent());
	}
}
