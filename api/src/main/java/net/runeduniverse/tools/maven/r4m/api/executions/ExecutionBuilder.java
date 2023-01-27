package net.runeduniverse.tools.runes4tools.maven.r4m.api.executions;

import java.util.List;
import java.util.Map;

import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.plugin.MojoExecution;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.filter.MvnPluginFilter;

public interface ExecutionBuilder {
	public ExecutionBuilder applyLifecycle(Lifecycle lifecycle);

	public ExecutionBuilder applyPluginFilter(MvnPluginFilter filter);

	public ExecutionBuilder applyExecutionId(String id);

	public Map<String, List<MojoExecution>> build();
}
