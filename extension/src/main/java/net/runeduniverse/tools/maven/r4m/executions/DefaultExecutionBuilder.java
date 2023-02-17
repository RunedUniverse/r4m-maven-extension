package net.runeduniverse.tools.maven.r4m.executions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.internal.builder.BuilderCommon;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.api.executions.ExecutionArchiveSubset;
import net.runeduniverse.tools.maven.r4m.api.executions.ExecutionBuilder;
import net.runeduniverse.tools.maven.r4m.api.executions.ForkMojoDescriptor;
import net.runeduniverse.tools.maven.r4m.api.executions.filter.MvnPluginFilter;
import net.runeduniverse.tools.maven.r4m.api.executions.model.Goal;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

public class DefaultExecutionBuilder implements ExecutionBuilder{

	protected final ExecutionArchiveSubset archive;
	protected final MavenProject mvnProject;
	protected final String executingLifecyclePhase;
	protected final ForkMojoDescriptor preForkMojo;
	protected final ForkMojoDescriptor postForkMojo;

	protected List<String> lifecyclePhases = new ArrayList<>();
	protected MvnPluginFilter mvnPluginFilter = null;
	protected String executionId = "";

	public DefaultExecutionBuilder(final ExecutionArchiveSubset archiveSubset, final MavenProject mvnProject,
			final String executingLifecyclePhase, final ForkMojoDescriptor preForkMojo,
			final ForkMojoDescriptor postForkMojo) {
		this.archive = archiveSubset;
		this.mvnProject = mvnProject;
		this.executingLifecyclePhase = executingLifecyclePhase;
		this.preForkMojo = preForkMojo;
		this.postForkMojo = postForkMojo;
	}

	/*
	 * Initialize mapping from lifecycle phase to bound mojos. The key set of this
	 * map denotes the phases the caller is interested in, i.e. all phases of the
	 * described Lifecycle.
	 */

	public ExecutionBuilder applyLifecycle(Lifecycle lifecycle) {
		// seed mappings with defined lifecycle phases
		if (lifecycle == null)
			this.lifecyclePhases = new ArrayList<>();
		else
			this.lifecyclePhases = lifecycle.getPhases();
		return this;
	}

	public ExecutionBuilder applyPluginFilter(MvnPluginFilter filter) {
		this.mvnPluginFilter = filter;
		return this;
	}

	public ExecutionBuilder applyExecutionId(String id) {
		this.executionId = id;
		return this;
	}

	public Map<String, List<MojoExecution>> build() {
		return this.buildMapping(this.convertExecutions(this.collectGoals()));
	}

	protected Map<String, List<Goal>> collectGoals() {
		Map<String, List<Goal>> phaseGoalMapping = new LinkedHashMap<>();
		Map<String, List<Goal>> phaseGoalExecMapping = this.archive
				.filterRegistry(this.mvnPluginFilter, (r4mPlugin) -> r4mPlugin.getExecution(this.executionId) != null)
				.phaseMapGoals(this.executionId);

		for (String phase : this.lifecyclePhases)
			phaseGoalMapping.put(phase, phaseGoalExecMapping.getOrDefault(phase, new LinkedList<>()));
		return phaseGoalMapping;
	}

	protected Map<String, Map<Integer, List<MojoExecution>>> convertExecutions(
			Map<String, List<Goal>> phaseGoalMapping) {
		Map<String, Map<Integer, List<MojoExecution>>> phaseExecutionMapping = new LinkedHashMap<>();
		Map<ForkMojoDescriptor, MojoExecution> forkMojos = new LinkedHashMap<>();

		if (this.preForkMojo != null)
			installForkMojo(phaseExecutionMapping, forkMojos, this.preForkMojo);
		installForkMojo(phaseExecutionMapping, forkMojos, this.postForkMojo);

		List<MojoExecution> forkExecutions = new LinkedList<>();
		for (Entry<String, List<Goal>> entry : phaseGoalMapping.entrySet()) {
			for (Goal goal : entry.getValue()) {
				MojoExecution mojo = new MojoExecution(goal.getDescriptor(), this.executionId,
						MojoExecution.Source.LIFECYCLE);
				mojo.setLifecyclePhase(entry.getKey());
				forkExecutions.add(mojo);
			}
		}

		forkMojos.get(this.postForkMojo)
				.setForkedExecutions(BuilderCommon.getKey(this.mvnProject), forkExecutions);
		return phaseExecutionMapping;
	}

	protected Map<String, List<MojoExecution>> buildMapping(
			Map<String, Map<Integer, List<MojoExecution>>> phaseMapping) {
		Map<String, List<MojoExecution>> mapping = new LinkedHashMap<>();

		for (Map.Entry<String, Map<Integer, List<MojoExecution>>> entry : phaseMapping.entrySet()) {
			List<MojoExecution> mojoExecutions = new ArrayList<>();

			for (List<MojoExecution> executions : entry.getValue()
					.values())
				mojoExecutions.addAll(executions);

			mapping.put(entry.getKey(), mojoExecutions);
		}
		return mapping;
	}

	protected void installForkMojo(final Map<String, Map<Integer, List<MojoExecution>>> phaseExecutionMapping,
			final Map<ForkMojoDescriptor, MojoExecution> forkMojos, final ForkMojoDescriptor forkMojo) {
		String execId = forkMojo.getExecutionId();
		MojoExecution mojo = new MojoExecution(forkMojo.getMvnPlugin(), forkMojo.getGoal(),
				isBlank(execId) ? this.executionId : execId);
		String forkPhase = forkMojo.getExecutionId();
		Map<Integer, List<MojoExecution>> executionTree = new TreeMap<>();
		phaseExecutionMapping.put(isBlank(forkPhase) ? this.executingLifecyclePhase : forkPhase, executionTree);
		List<MojoExecution> forkMojoExecutions = new ArrayList<>();
		executionTree.put(0, forkMojoExecutions);
		forkMojoExecutions.add(mojo);
		forkMojos.put(forkMojo, mojo);
	}
}
