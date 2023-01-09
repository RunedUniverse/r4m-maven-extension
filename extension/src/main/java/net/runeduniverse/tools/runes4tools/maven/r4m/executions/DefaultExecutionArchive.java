package net.runeduniverse.tools.runes4tools.maven.r4m.executions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.ExecutionArchive;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.ExecutionArchiveSubset;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.ForkMojoDescriptor;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.filter.MvnPluginFilter;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.filter.R4mPluginFilter;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model.Execution;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model.Goal;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model.Phase;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model.Plugin;

@Component(role = ExecutionArchive.class, instantiationStrategy = "keep-alive")
public class DefaultExecutionArchive implements ExecutionArchive {

	private Map<org.apache.maven.model.Plugin, Plugin> pluginRegistry = new LinkedHashMap<>();
	private Map<org.apache.maven.model.Plugin, Map<String, Boolean>> pluginRegistryFlags = new LinkedHashMap<>();

	public DefaultExecutionArchive() {
	}

	@Override
	public boolean isRegistered(org.apache.maven.model.Plugin mvnPlugin) {
		return this.pluginRegistry.containsKey(mvnPlugin);
	}

	@Override
	public boolean isRegisteredWithFlag(org.apache.maven.model.Plugin mvnPlugin, String flag) {
		Map<String, Boolean> flagMap = this.pluginRegistryFlags.get(mvnPlugin);
		if (flagMap == null)
			return false;
		return flagMap.containsKey(flag);
	}

	@Override
	public boolean isRegisteredWithFlag(org.apache.maven.model.Plugin mvnPlugin, String flag, Boolean flagValue) {
		Map<String, Boolean> flagMap = this.pluginRegistryFlags.get(mvnPlugin);
		if (flagMap == null)
			return false;
		if (!flagMap.containsKey(flag))
			return false;
		return flagMap.get(flag)
				.equals(flagValue);
	}

	@Override
	public ExecutionArchive setFlag(org.apache.maven.model.Plugin mvnPlugin, String flag, Boolean flagValue) {
		if (!this.pluginRegistry.containsKey(mvnPlugin))
			return this;
		Map<String, Boolean> flagMap = this.pluginRegistryFlags.get(mvnPlugin);
		if (flagMap == null) {
			flagMap = new HashMap<>();
			this.pluginRegistryFlags.put(mvnPlugin, flagMap);
		}
		flagMap.put(flag, flagValue);
		return this;
	}

	@Override
	public ExecutionArchive removeFlag(org.apache.maven.model.Plugin mvnPlugin, String flag) {
		Map<String, Boolean> flagMap = this.pluginRegistryFlags.get(mvnPlugin);
		if (flagMap == null)
			return null;
		flagMap.remove(flag);
		return this;
	}

	@Override
	public Plugin getPlugin(org.apache.maven.model.Plugin mvnPlugin) {
		return this.pluginRegistry.get(mvnPlugin);
	}

	@Override
	public Plugin getPluginOrDefault(org.apache.maven.model.Plugin mvnPlugin, Plugin r4mPlugin) {
		Plugin plugin = this.pluginRegistry.get(mvnPlugin);
		if (plugin == null)
			return r4mPlugin;
		return plugin;
	}

	@Override
	public Boolean getFlag(org.apache.maven.model.Plugin mvnPlugin, String flag) {
		Map<String, Boolean> flagMap = this.pluginRegistryFlags.get(mvnPlugin);
		if (flagMap == null)
			return null;
		return flagMap.get(flag);
	}

	@Override
	public ExecutionArchive register(org.apache.maven.model.Plugin mvnPlugin, Plugin r4mPlugin) {
		this.pluginRegistry.put(mvnPlugin, r4mPlugin);
		this.pluginRegistryFlags.put(mvnPlugin, new HashMap<>());
		return this;
	}

	@Override
	public ExecutionArchive remove(org.apache.maven.model.Plugin mvnPlugin) {
		this.pluginRegistry.remove(mvnPlugin);
		this.pluginRegistryFlags.remove(mvnPlugin);
		return this;
	}

	@Override
	public ExecutionArchive clear() {
		this.pluginRegistry.clear();
		this.pluginRegistryFlags.clear();
		return this;
	}

	@Override
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
