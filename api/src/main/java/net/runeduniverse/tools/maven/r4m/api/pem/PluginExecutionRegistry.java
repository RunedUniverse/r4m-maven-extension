package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem;

import org.apache.maven.plugin.descriptor.PluginDescriptor;

public interface PluginExecutionRegistry {

	PluginExecutionRegistrySlice createSlice(PluginDescriptor mvnPluginDescriptor);

	boolean hasSlice(String prefix);

	boolean hasSlice(String groupId, String artifactId);

	PluginExecutionRegistrySlice getSlice(String prefix);

	PluginExecutionRegistrySlice getSlice(String groupId, String artifactId);
}
