package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public interface ExecutionArchiveSlice extends Recordable {
	public MavenProject getMvnProject();

	public String getVersion();

	public ExecutionArchiveSlice getParent();

	public ProjectExecutionModel getModel(Execution execution);

	public Set<Execution> getExecutions();

	public Set<Execution> getExecutions(final ExecutionFilter filter, final boolean onlyInherited);

	public Set<Execution> getEffectiveExecutions(final ExecutionFilter filter, final boolean onlyInherited);

	public void setParent(ExecutionArchiveSlice parent);

	public void register(ProjectExecutionModel pem);
}
