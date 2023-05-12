package net.runeduniverse.tools.maven.r4m.pem.api;

import net.runeduniverse.tools.maven.r4m.pem.model.Execution;

@FunctionalInterface
public interface ExecutionFilter {

	public boolean apply(Execution execution);
}
