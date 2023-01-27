package net.runeduniverse.tools.maven.r4m.api.pem;

import java.util.Collection;
import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;

public interface ExecutionArchiveSlice {
	public MavenProject getMvnProject();

	public String getVersion();

	public ExecutionArchiveSlice getParent();

	public Set<Execution> getExecutions(ExecutionFilter filter);

	public void setParent(ExecutionArchiveSlice parent);

	public void register(Collection<Execution> executions);
}
