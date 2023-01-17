package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem;

import java.util.Collection;
import java.util.Set;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Execution;

public interface PluginExecutionRegistrySlice {
	public Set<Execution> getExecutions();

	public void addExecutions(Collection<Execution> values);
}
