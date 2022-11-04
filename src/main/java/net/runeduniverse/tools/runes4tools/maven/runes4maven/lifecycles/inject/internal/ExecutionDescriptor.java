package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal;

import java.util.LinkedHashMap;
import java.util.Map;

import net.runeduniverse.tools.runes4tools.maven.runes4maven.Properties;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model.Execution;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model.Plugin;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

public class ExecutionDescriptor {

	protected final Map<String, Execution> executions;

	public ExecutionDescriptor() {
		this.executions = new LinkedHashMap<>();
	}

	public ExecutionDescriptor(Map<String, Execution> executions) {
		this.executions = executions;
	}

	Execution getExecutionById(String id) {
		return this.executions.get(id);
	}

	public Execution put(Execution execution) {
		String id = execution.getId();
		if (isBlank(id))
			id = Properties.DEFAULT_EXECUTION_ID;
		return this.executions.put(id, execution);
	}

	public void populate(Plugin plugin) {
		for (Execution item : executions.values())
			plugin.putExecution(item);
	}
}
