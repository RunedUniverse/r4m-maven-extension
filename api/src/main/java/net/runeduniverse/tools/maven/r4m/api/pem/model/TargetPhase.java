package net.runeduniverse.tools.maven.r4m.api.pem.model;

import java.util.LinkedHashSet;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.Recordable;

public class TargetPhase implements Recordable {

	private String id;
	private final Set<String> executions = new LinkedHashSet<>(0);

	public TargetPhase(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public Set<String> getExecutions() {
		return this.executions;
	}

	public void addExecutions(Set<String> executions) {
		this.executions.addAll(executions);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof TargetPhase))
			return false;
		TargetPhase phase = (TargetPhase) obj;

		if (!(this.executions.size() == phase.getExecutions()
				.size() && this.executions.containsAll(phase.getExecutions())))
			return false;

		return true;
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("Target Phase");

		tree.append("id", this.id);

		if (!this.executions.isEmpty())
			tree.append("executions", '[' + String.join(", ", this.executions) + ']');

		return tree;
	}
}
