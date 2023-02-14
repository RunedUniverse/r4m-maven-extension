package net.runeduniverse.tools.maven.r4m.api.pem.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.Recordable;

public class Fork implements Recordable {
	private String mode = null;
	private final Set<String> executions = new LinkedHashSet<>(0);
	private TargetLifecycle lifecycle = null;
	private List<TargetPhase> phases = null;
	private final List<TargetPhase> excludePhases = new LinkedList<>();

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

	public List<TargetPhase> getExcludedPhases() {
		return this.excludePhases;
	}

	public boolean isValid() {
		if (this.mode != null)
			return true;
		if (!this.executions.isEmpty())
			return true;
		if (this.lifecycle != null)
			return true;
		if (!this.phases.isEmpty())
			return true;
		// this.excludePhases are irrelevant
		return false;
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

	public void addExcludedPhases(Collection<TargetPhase> excludedPhases) {
		// check for uniquity by equals
		for (TargetPhase phase : excludedPhases)
			if (!this.excludePhases.contains(phase))
				this.excludePhases.add(phase);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Fork))
			return false;
		Fork fork = (Fork) obj;

		if (!this.mode.equals(fork.getMode()))
			return false;

		if (this.lifecycle == null) {
			if (fork.getLifecycle() != null)
				return false;
		} else if (!this.lifecycle.equals(fork.getLifecycle()))
			return false;

		if (!(this.executions.size() == fork.getExecutions()
				.size() && this.executions.containsAll(fork.getExecutions())))
			return false;

		// Lists check using equals & also check the order of elements
		if (this.phases == null) {
			if (fork.getPhases() != null)
				return false;
		} else if (!this.phases.equals(fork.getPhases()))
			return false;

		// Lists check using equals through containsAll
		if (!(this.excludePhases.size() == fork.getExcludedPhases()
				.size() && this.excludePhases.containsAll(fork.getExcludedPhases())))
			return false;

		return super.equals(obj);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("Fork");

		if (!this.isValid()) {
			tree.append("[WARNING]", "insufficient information -> will be ignored!");
			return tree;
		}

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
