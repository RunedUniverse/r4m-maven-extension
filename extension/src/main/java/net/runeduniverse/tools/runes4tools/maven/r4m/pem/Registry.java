package net.runeduniverse.tools.runes4tools.maven.r4m.pem;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.PluginExecutionRegistry;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.PluginExecutionRegistrySlice;

@Component(role = PluginExecutionRegistry.class, instantiationStrategy = "keep-alive")
public class Registry implements PluginExecutionRegistry {

	@Override
	public PluginExecutionRegistrySlice createSlice() {
		// TODO Auto-generated method stub
		return null;
	}

}
