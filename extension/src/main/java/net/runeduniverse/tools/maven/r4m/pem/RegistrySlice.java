package net.runeduniverse.tools.maven.r4m.pem;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.runeduniverse.tools.maven.r4m.pem.api.PluginExecutionRegistrySlice;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public class RegistrySlice implements PluginExecutionRegistrySlice {

	// save more infos???

	private Set<Execution> executions = new LinkedHashSet<>(0);
	private Map<Execution, ProjectExecutionModel> executionOrigins = new LinkedHashMap<>(0);
	private Map<String, ProjectExecutionModel> hintedOrigins = new LinkedHashMap<>(0);

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
		if (model == null || model.getExecutions()
				.isEmpty())
			return;

		this.hintedOrigins.put(createKey(model.getParserType(), model.getParserHint()), model);

		for (Execution execution : model.getExecutions()) {
			this.executions.add(execution);
			this.executionOrigins.put(execution, model);
		}
	}

	@Override
	public ProjectExecutionModel getModel(Class<?> parserType, String parserHint) {
		return this.hintedOrigins.get(createKey(parserType, parserHint));
	}

	@Override
	public Set<ProjectExecutionModel> getModels() {
		return new LinkedHashSet<>(this.hintedOrigins.values());
	}

	protected static String createKey(Class<?> parserType, String parserHint) {
		if (isBlank(parserHint))
			parserHint = "default";
		if (parserType == null)
			return parserHint;
		return String.join(":", parserType.getCanonicalName(), parserHint);
	}

}
