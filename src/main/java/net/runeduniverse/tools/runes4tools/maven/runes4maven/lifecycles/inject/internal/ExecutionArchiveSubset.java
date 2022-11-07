package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal;

import java.util.List;
import java.util.Map;

import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.filter.MvnPluginFilter;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.filter.R4mPluginFilter;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model.Goal;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model.Plugin;

public interface ExecutionArchiveSubset {
	public boolean contains(org.apache.maven.model.Plugin mvnPlugin);

	public boolean contains(Plugin r4mPlugin);

	public ExecutionArchiveSubset newSubset();

	public ExecutionArchiveSubset filterRegistry(MvnPluginFilter mvnFilter);

	public ExecutionArchiveSubset filterRegistry(R4mPluginFilter r4mFilter);

	public ExecutionArchiveSubset filterRegistry(MvnPluginFilter mvnFilter, R4mPluginFilter r4mFilter);

	public Map<String, List<Goal>> phaseMapGoals(String executionId);
}
