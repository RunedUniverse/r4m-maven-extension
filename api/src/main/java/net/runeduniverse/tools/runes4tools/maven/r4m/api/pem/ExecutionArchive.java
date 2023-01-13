package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem;

import org.apache.maven.project.MavenProject;

public interface ExecutionArchive {
	public ExecutionArchiveSlice createSlice(final MavenProject mvnProject);

	public ExecutionArchiveSlice getSlice(MavenProject mvnProject);

	public ExecutionArchiveSelector newSelection();
}
