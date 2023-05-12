package net.runeduniverse.tools.maven.r4m.pem.api;

import org.apache.maven.project.MavenProject;

public interface ExecutionArchive extends Recordable {
	public ExecutionArchiveSlice createSlice(final MavenProject mvnProject);

	public ExecutionArchiveSlice getSlice(final MavenProject mvnProject);
}
