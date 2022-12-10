package net.runeduniverse.tools.runes4tools.maven.r4m.api.executions;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model.Plugin;
import net.runeduniverse.tools.runes4tools.maven.r4m.executions.ExecutionBuilder;

public interface ExecutionArchive {
	public boolean contains(org.apache.maven.model.Plugin mvnPlugin);

	public ExecutionArchive register(org.apache.maven.model.Plugin mvnPlugin, Plugin r4mPlugin);

	public ExecutionArchiveSubset newSubset();

	public ExecutionBuilder createBuilder(final MavenProject mvnProject, final String executingLifecyclePhase,
			final ForkMojoDescriptor preForkMojo, final ForkMojoDescriptor postForkMojo);
}
