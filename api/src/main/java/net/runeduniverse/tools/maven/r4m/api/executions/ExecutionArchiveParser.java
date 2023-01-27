package net.runeduniverse.tools.maven.r4m.api.executions;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

public interface ExecutionArchiveParser {
	void parsePlugin(final ExecutionArchive archive, final MavenSession mvnSession, final MavenProject mvnProject,
			org.apache.maven.model.Plugin mvnPlugin);
}
