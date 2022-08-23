package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.filter.MvnPluginFilter;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model.Plugin;

public class ExecutionArchive {

	private Map<org.apache.maven.model.Plugin, Plugin> pluginRegistry = new LinkedHashMap<>();

	public ExecutionArchive() {
	}

	public boolean contains(org.apache.maven.model.Plugin plugin) {
		return this.pluginRegistry.containsKey(plugin);
	}

	public ExecutionArchive register(org.apache.maven.model.Plugin mvnPlugin, Plugin plugin) {
		this.pluginRegistry.put(mvnPlugin, plugin);
		return this;
	}

	public Map<org.apache.maven.model.Plugin, Plugin> filterRegistry(MvnPluginFilter filter) {
		Map<org.apache.maven.model.Plugin, Plugin> plugins = new LinkedHashMap<>();

		for (Entry<org.apache.maven.model.Plugin, Plugin> entry : this.pluginRegistry.entrySet()) {
			if (filter.apply(entry.getKey()))
				plugins.put(entry.getKey(), entry.getValue());
		}
		return plugins;
	}

	public ExecutionBuilder createBuilder(MavenProject mvnProject) {
		return new ExecutionBuilder(this, mvnProject);
	}
}
