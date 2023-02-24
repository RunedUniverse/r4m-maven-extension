package net.runeduniverse.tools.maven.r4m.api.pem;

import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

public interface ExecutionArchiveSlice extends Recordable {
	public MavenProject getMvnProject();

	public String getVersion();

	public ExecutionArchiveSlice getParent();

	public ProjectExecutionModel getModel(Execution execution);

	public Set<Execution> getExecutions();

	public Set<Execution> getExecutions(ExecutionFilter filter);

	public Set<Execution> getEffectiveExecutions(ExecutionFilter filter);

	public void setParent(ExecutionArchiveSlice parent);

	public void register(ProjectExecutionModel pem);
}
