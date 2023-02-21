package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.runeduniverse.tools.maven.r4m.api.pem.PluginExecutionRegistrySlice;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

public class RegistrySlice implements PluginExecutionRegistrySlice {

	// save more infos???

	private Set<Execution> executions = new LinkedHashSet<>(0);
	private Map<Execution, ProjectExecutionModel> executionOrigins = new LinkedHashMap<>(0);

	@Override
	public Set<Execution> getExecutions() {
		return Collections.unmodifiableSet(this.executions);
	}

	@Override
	public void addExecutions(Collection<Execution> values) {
		this.executions.addAll(values);
	}

	@Override
	public void includeModel(ProjectExecutionModel model) {
		if (model == null)
			return;

		for (Execution execution : model.getExecutions()) {
			this.executions.add(execution);
			this.executionOrigins.put(execution, model);
		}
	}

}
