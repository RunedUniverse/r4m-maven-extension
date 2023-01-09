package net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;

public class ExecutionDescriptor {

	protected final CompoundTree tree = new CompoundTree("Execution Tree");
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
		execution.toRecord(tree);
		return this.executions.put(execution.getId(), execution);
	}

	public Collection<Execution> getExecutions() {
		return this.executions.values();
	}

	@Override
	public String toString() {
		return this.tree.toString();
	}
}
