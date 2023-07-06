package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.Collection;
import java.util.Set;

import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public interface PluginExecutionRegistrySlice {

	public Set<Execution> getExecutions();

	public void addExecutions(Collection<Execution> values);

	public void includeModel(ProjectExecutionModel model);

	public ProjectExecutionModel getModel(Class<?> parserType, String parserHint);

	public Set<ProjectExecutionModel> getModels();

}
