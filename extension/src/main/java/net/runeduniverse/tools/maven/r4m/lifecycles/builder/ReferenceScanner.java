package net.runeduniverse.tools.maven.r4m.lifecycles.builder;

import java.util.Set;

import org.apache.maven.plugin.logging.Log;

public interface ReferenceScanner {

	public ReferenceScanner inject(BuilderRuntime runtime);

	public ReferenceScanner inject(Set<ReferenceFileScanner> fileScanner);

	public ReferenceScanner inject(ReferenceMap resultMap);

	public boolean logInfo(Log log);

	public boolean logAnalisis(Log log);

}
