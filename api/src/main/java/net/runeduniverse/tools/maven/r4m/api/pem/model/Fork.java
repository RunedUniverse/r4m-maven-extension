package net.runeduniverse.tools.maven.r4m.api.pem.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Fork {
	private String mode = null;
	private Set<String> executions = new LinkedHashSet<>(0);
	private TargetLifecycle lifecycle = null;
	private List<TargetPhase> phases = null;
	private Set<TargetPhase> excludePhases = new LinkedHashSet<>(0);

	public TargetLifecycle getLifecycle() {
		return this.lifecycle;
	}

	public String getMode() {
		return this.mode;
	}

	public Set<String> getExecutions() {
		return this.executions;
	}

	public List<TargetPhase> getPhases() {
		return this.phases;
	}

	public Set<TargetPhase> getExcludedPhases() {
		return this.excludePhases;
	}

	public void setLifecycle(TargetLifecycle lifecycle) {
		this.lifecycle = lifecycle;
	}

	public void setMode(String value) {
		this.mode = value;
	}

	public void addExecutions(Collection<String> executions) {
		this.executions.addAll(executions);
	}

	public void setPhases(List<TargetPhase> phases) {
		this.phases = phases;
	}

	public void addExcludedPhases(Set<TargetPhase> excludedPhases) {
		this.excludePhases.addAll(excludedPhases);
	}

}
