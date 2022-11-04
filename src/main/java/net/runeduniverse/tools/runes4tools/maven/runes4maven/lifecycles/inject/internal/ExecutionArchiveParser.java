package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

public interface ExecutionArchiveParser {
	void parsePlugin(final ExecutionArchive archive, final MavenSession mvnSession, final MavenProject mvnProject,
			org.apache.maven.model.Plugin mvnPlugin);
}
