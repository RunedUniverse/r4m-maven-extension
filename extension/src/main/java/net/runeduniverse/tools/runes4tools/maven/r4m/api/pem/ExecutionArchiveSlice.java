package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem;

import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Execution;

public interface ExecutionArchiveSlice {
	public MavenProject getMvnProject();

	public String getVersion();

	public ExecutionArchiveSlice getParent();

	public Set<Execution> getExecutions(ExecutionFilter filter);
}
