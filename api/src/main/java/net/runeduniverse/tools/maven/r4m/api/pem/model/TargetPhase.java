package net.runeduniverse.tools.maven.r4m.api.pem.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class TargetPhase {

	private String id;
	private Set<String> executions = new LinkedHashSet<>(0);

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
}
