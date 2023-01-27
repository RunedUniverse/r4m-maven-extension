package net.runeduniverse.tools.maven.r4m.api.pem;

import org.apache.maven.project.MavenProject;

public interface ExecutionArchive {
	public ExecutionArchiveSlice createSlice(final MavenProject mvnProject);

	public ExecutionArchiveSlice getSlice(final MavenProject mvnProject);

	public ExecutionArchiveSelector newSelection();
}
