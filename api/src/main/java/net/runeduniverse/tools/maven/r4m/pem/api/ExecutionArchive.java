package net.runeduniverse.tools.maven.r4m.pem.api;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.logging.logs.Recordable;

public interface ExecutionArchive extends Recordable {

	public ExecutionArchiveSlice createSlice(final MavenProject mvnProject);

	public ExecutionArchiveSlice getSlice(final MavenProject mvnProject);

}
