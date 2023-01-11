package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Execution;

@FunctionalInterface
public interface ExecutionFilter {

	public boolean apply(Execution execution);
}
