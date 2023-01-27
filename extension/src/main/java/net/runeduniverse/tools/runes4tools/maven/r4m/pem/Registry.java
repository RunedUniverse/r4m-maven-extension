package net.runeduniverse.tools.runes4tools.maven.r4m.pem;

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.PluginExecutionRegistry;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.PluginExecutionRegistrySlice;

@Component(role = PluginExecutionRegistry.class, instantiationStrategy = "keep-alive")
public class Registry implements PluginExecutionRegistry {

	@Override
	public PluginExecutionRegistrySlice createSlice(PluginDescriptor mvnPluginDescriptor) {
		// TODO index descriptor

		PluginExecutionRegistrySlice slice = new RegistrySlice();
		// TODO index slice

		return slice;
	}

	@Override
	public boolean hasSlice(String prefix) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasSlice(String groupId, String artifactId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PluginExecutionRegistrySlice getSlice(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PluginExecutionRegistrySlice getSlice(String groupId, String artifactId) {
		// TODO Auto-generated method stub
		return null;
	}

}
