package net.runeduniverse.tools.maven.r4m.lifecycle.api;

import org.apache.maven.lifecycle.internal.LifecycleTask;

public interface LifecycleTaskParser {

	public LifecycleTaskData parse(LifecycleTask task);

}
