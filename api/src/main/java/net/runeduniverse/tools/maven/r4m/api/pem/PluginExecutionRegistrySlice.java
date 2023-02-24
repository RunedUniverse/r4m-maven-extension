package net.runeduniverse.tools.maven.r4m.api.pem;

import java.util.Collection;
import java.util.Set;

import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

public interface PluginExecutionRegistrySlice {
	public Set<Execution> getExecutions();

	public void addExecutions(Collection<Execution> values);

	public void includeModel(ProjectExecutionModel model);

	public ProjectExecutionModel getModel(String parserHint);

	public Set<ProjectExecutionModel> getModels();
}
