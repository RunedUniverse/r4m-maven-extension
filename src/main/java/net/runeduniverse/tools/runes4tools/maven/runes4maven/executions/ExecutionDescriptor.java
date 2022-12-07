package net.runeduniverse.tools.runes4tools.maven.runes4maven.executions;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

//import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.model.Execution;

public class ExecutionDescriptor {

	//protected final CompoundTree record = new CompoundTree("Execution Tree");
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
		//execution.toRecord(record);
		return this.executions.put(execution.getId(), execution);
	}

	public Collection<Execution> getExecutions() {
		return this.executions.values();
	}

	/*@Override
	public String toString() {
		return this.record.toString();
	}*/
}
