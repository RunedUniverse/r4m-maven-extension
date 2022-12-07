package net.runeduniverse.tools.runes4tools.maven.runes4maven.executions;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.ExecutionArchive;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.ExecutionArchiveSubset;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.ForkMojoDescriptor;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.filter.MvnPluginFilter;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.filter.R4mPluginFilter;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.model.Execution;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.model.Goal;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.model.Phase;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.model.Plugin;

public class DefaultExecutionArchive implements ExecutionArchive {

	private Map<org.apache.maven.model.Plugin, Plugin> pluginRegistry = new LinkedHashMap<>();

	public DefaultExecutionArchive() {
	}

	public boolean contains(org.apache.maven.model.Plugin mvnPlugin) {
		return this.pluginRegistry.containsKey(mvnPlugin);
	}

	public ExecutionArchive register(org.apache.maven.model.Plugin mvnPlugin, Plugin r4mPlugin) {
		this.pluginRegistry.put(mvnPlugin, r4mPlugin);
		return this;
	}

	public ExecutionArchiveSubset newSubset() {
		return new Subset(this.pluginRegistry);
	}

	public ExecutionBuilder createBuilder(final MavenProject mvnProject, final String executingLifecyclePhase,
			final ForkMojoDescriptor preForkMojo, final ForkMojoDescriptor postForkMojo) {
		return new ExecutionBuilder(this.newSubset(), mvnProject, executingLifecyclePhase, preForkMojo, postForkMojo);
	}

	private class Subset implements ExecutionArchiveSubset {
		private Map<org.apache.maven.model.Plugin, Plugin> linkedRegistry;

		protected Subset(Map<org.apache.maven.model.Plugin, Plugin> linkedRegistry) {
			this.linkedRegistry = new LinkedHashMap<>(linkedRegistry);
		}

		@Override
		public boolean contains(org.apache.maven.model.Plugin mvnPlugin) {
			return this.linkedRegistry.containsKey(mvnPlugin);
		}

		@Override
		public boolean contains(Plugin r4mPlugin) {
			return this.linkedRegistry.containsValue(r4mPlugin);
		}

		@Override
		public ExecutionArchiveSubset newSubset() {
			return new Subset(this.linkedRegistry);
		}

		@Override
		public ExecutionArchiveSubset filterRegistry(MvnPluginFilter mvnFilter) {
			return this.filterRegistry(mvnFilter, (f) -> true);
		}

		@Override
		public ExecutionArchiveSubset filterRegistry(R4mPluginFilter r4mFilter) {
			return this.filterRegistry((f) -> true, r4mFilter);
		}

		@Override
		public ExecutionArchiveSubset filterRegistry(MvnPluginFilter mvnFilter, R4mPluginFilter r4mFilter) {
			Map<org.apache.maven.model.Plugin, Plugin> filteredRegistry = new LinkedHashMap<>();

			for (Entry<org.apache.maven.model.Plugin, Plugin> entry : this.linkedRegistry.entrySet()) {
				if (mvnFilter.apply(entry.getKey()) && r4mFilter.apply(entry.getValue()))
					filteredRegistry.put(entry.getKey(), entry.getValue());
			}
			this.linkedRegistry = filteredRegistry;

			return this;
		}

		@Override
		public Map<String, List<Goal>> phaseMapGoals(String executionId) {
			Map<String, List<Goal>> map = new LinkedHashMap<>();

			for (Plugin r4mPlugin : this.linkedRegistry.values()) {
				Execution exec = r4mPlugin.getExecution(executionId);
				if (exec == null)
					continue;
				for (Phase phase : exec.getPhases()
						.values()) {
					List<Goal> phaseList = map.get(phase.getId());
					if (phaseList == null) {
						phaseList = new LinkedList<>();
						map.put(phase.getId(), phaseList);
					}
					phaseList.addAll(phase.getGoals());
				}
			}

			return map;
		}
	}
}
