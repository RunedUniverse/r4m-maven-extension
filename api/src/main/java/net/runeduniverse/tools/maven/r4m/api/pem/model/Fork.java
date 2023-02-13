package net.runeduniverse.tools.maven.r4m.api.pem.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.Recordable;

public class Fork implements Recordable {
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

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("Fork");

		tree.append("mode", this.mode == null ? "inherited" : this.mode);

		if (!this.executions.isEmpty())
			tree.append("executions", '[' + String.join(", ", this.executions) + ']');

		if (this.lifecycle != null)
			tree.append(this.lifecycle.toRecord());

		if (this.phases != null && !this.phases.isEmpty()) {
			CompoundTree phasesTree = new CompoundTree("phases");
			for (TargetPhase phase : this.phases)
				phasesTree.append(phase.toRecord());
			tree.append(phasesTree);
		}

		if (!this.excludePhases.isEmpty()) {
			CompoundTree phasesTree = new CompoundTree("excluded phases");
			for (TargetPhase phase : this.excludePhases)
				phasesTree.append(phase.toRecord());
			tree.append(phasesTree);
		}

		return tree;
	}

}
