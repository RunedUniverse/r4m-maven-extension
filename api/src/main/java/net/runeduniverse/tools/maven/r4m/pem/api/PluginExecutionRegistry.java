package net.runeduniverse.tools.maven.r4m.pem.api;

import org.apache.maven.plugin.descriptor.PluginDescriptor;

public interface PluginExecutionRegistry {

	PluginExecutionRegistrySlice createSlice(PluginDescriptor mvnPluginDescriptor);

	boolean hasSlice(String prefix);

	boolean hasSlice(String groupId, String artifactId);

	PluginExecutionRegistrySlice getSlice(String prefix);

	PluginExecutionRegistrySlice getSlice(String groupId, String artifactId);

}
