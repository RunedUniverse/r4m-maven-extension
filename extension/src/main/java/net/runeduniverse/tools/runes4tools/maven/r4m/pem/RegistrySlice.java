package net.runeduniverse.tools.runes4tools.maven.r4m.pem;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.PluginExecutionRegistrySlice;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Execution;

public class RegistrySlice implements PluginExecutionRegistrySlice {

	// TODO must save waaaay more infos!!!

	private Set<Execution> executions = new LinkedHashSet<>(0);

	@Override
	public Set<Execution> getExecutions() {
		return Collections.unmodifiableSet(this.executions);
	}

	@Override
	public void addExecutions(Collection<Execution> values) {
		this.executions.addAll(values);
	}

}
