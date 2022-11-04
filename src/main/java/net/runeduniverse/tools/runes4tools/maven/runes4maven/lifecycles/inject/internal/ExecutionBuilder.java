package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.runes4tools.maven.runes4maven.Properties;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.filter.MvnPluginFilter;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model.Goal;

public class ExecutionBuilder {

	protected final ExecutionArchive archive;
	protected final MavenProject mvnProject;

	protected List<String> lifecyclePhases = new ArrayList<>();
	protected MvnPluginFilter mvnPluginFilter = null;
	protected String executionId = Properties.LIFECYCLE.INJECT.DEFAULT_EXECUTION_ID;

	public ExecutionBuilder(ExecutionArchive archive, MavenProject mvnProject) {
		this.archive = archive;
		this.mvnProject = mvnProject;
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
		for (String phase : this.lifecyclePhases) {
			List<Goal> goals = new LinkedList<>();

			// TODO collect goals
			phaseGoalMapping.put(phase, goals);
		}
		return phaseGoalMapping;
	}

	protected Map<String, Map<Integer, List<MojoExecution>>> convertExecutions(
			Map<String, List<Goal>> phaseGoalMapping) {
		Map<String, Map<Integer, List<MojoExecution>>> phaseExecutionMapping = new LinkedHashMap<>();
		for (Entry<String, List<Goal>> entry : phaseGoalMapping.entrySet()) {
			Map<Integer, List<MojoExecution>> executionTree = new TreeMap<>();

			// TODO resolve/create MojoExecutions

			// for Goal
			//MojoDescriptor mojoDescriptor = pluginManager.getMojoDescriptor(plugin, goal,	project.getRemotePluginRepositories(), session.getRepositorySession());

			//MojoExecution mojoExecution = new MojoExecution(mojoDescriptor, descriptor.getExecutionId(), MojoExecution.Source.LIFECYCLE);

			//

			phaseExecutionMapping.put(entry.getKey(), executionTree);
		}

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
}
