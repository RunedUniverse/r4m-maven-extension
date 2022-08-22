package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model.Plugin;

public class ExecutionArchive {

	private Set<Plugin> plugins = new LinkedHashSet<>();
	private Map<org.apache.maven.model.Plugin, Plugin> pluginMappingRegistry = new LinkedHashMap<>();

	public ExecutionArchive() {
	}
	
	public boolean contains(org.apache.maven.model.Plugin plugin) {
		return this.pluginMappingRegistry.containsKey(plugin);
	}
	
	public ExecutionArchive register(org.apache.maven.model.Plugin mvnPlugin, Plugin plugin) {
		this.pluginMappingRegistry.put(mvnPlugin, plugin);
		this.plugins.add(plugin);
		return this;
	}
}
