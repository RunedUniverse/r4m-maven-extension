package net.runeduniverse.tools.maven.r4m.api.executions;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.api.executions.model.Plugin;

public interface ExecutionArchive {
	public boolean isRegistered(org.apache.maven.model.Plugin mvnPlugin);

	public boolean isRegisteredWithFlag(org.apache.maven.model.Plugin mvnPlugin, String flag);

	public boolean isRegisteredWithFlag(org.apache.maven.model.Plugin mvnPlugin, String flag, Boolean flagValue);

	public ExecutionArchive register(org.apache.maven.model.Plugin mvnPlugin, Plugin r4mPlugin);

	public ExecutionArchive remove(org.apache.maven.model.Plugin mvnPlugin);

	public ExecutionArchive clear();

	public ExecutionArchive setFlag(org.apache.maven.model.Plugin mvnPlugin, String flag, Boolean flagValue);

	public ExecutionArchive removeFlag(org.apache.maven.model.Plugin mvnPlugin, String flag);

	public Plugin getPlugin(org.apache.maven.model.Plugin mvnPlugin);

	public Plugin getPluginOrDefault(org.apache.maven.model.Plugin mvnPlugin, Plugin r4mPlugin);

	public Boolean getFlag(org.apache.maven.model.Plugin mvnPlugin, String flag);

	public ExecutionArchiveSubset newSubset();

	public ExecutionBuilder createBuilder(final MavenProject mvnProject, final String executingLifecyclePhase,
			final ForkMojoDescriptor preForkMojo, final ForkMojoDescriptor postForkMojo);
}
