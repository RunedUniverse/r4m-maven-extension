package net.runeduniverse.tools.maven.r4m.mojos;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

		/*
		 * getLog().warn("all"); for (Execution execution : executions) {
		 * getLog().info(String.format("id: %s\tsource: %s\tpackaging: [%s]",
		 * execution.getId(), execution.getSource(), String.join(", ",
		 * execution.getPackagingProcedures()))); }
		 */
		getLog().warn("reduced");
		for (Map<ExecutionSource, List<Execution>> x : map.values()) {
			for (List<Execution> y : x.values()) {
				for (Execution execution : y) {
					getLog().info(String.format("id: %s\tsource: %s\tpackaging: [%s]", execution.getId(),
							execution.getSource(), String.join(", ", execution.getPackagingProcedures())));
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
					exec.addLifecycle(lifecycle);
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

	private Execution getEquivalent(final List<Execution> pool, final Execution original) {
		for (Execution exec : pool) {
			// id
			final String id = original.getId();
			if (id == null) {
				if (exec.getId() != null)
					continue;
			} else if (!id.equals(exec.getId()))
				continue;
			// source
			final ExecutionSource source = original.getSource();
			if (source == null) {
				if (exec.getSource() != null)
					continue;
			} else if (!source.equals(exec.getSource()))
				continue;
			// active flags
			if (original.isAlwaysActive() != exec.isAlwaysActive()
					|| original.isDefaultActive() != exec.isDefaultActive()
					|| original.isNeverActive() != exec.isNeverActive())
				continue;
			// packagingProcedures
			final Set<String> execPackagingProcedures = exec.getPackagingProcedures();
			if (execPackagingProcedures.size() != original.getPackagingProcedures()
					.size())
				continue;
			if (!execPackagingProcedures.containsAll(original.getPackagingProcedures()))
				continue;
			// trigger
			final List<Trigger> execTrigger = new LinkedList<>(exec.getTrigger());
			if (execTrigger.size() != original.getTrigger()
					.size())
				continue;
			if (!execTrigger.containsAll(original.getTrigger()))
				continue;
			// equal metadata
			return exec;
		}
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
