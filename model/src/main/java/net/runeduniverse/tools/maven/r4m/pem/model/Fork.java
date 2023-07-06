/*
 * Copyright © 2023 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.tools.maven.r4m.pem.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;

public class Fork implements Recordable {

	private String mode = null;
	private final Set<String> executions = new LinkedHashSet<>(0);
	private TargetLifecycle lifecycle = null;
	private List<TargetPhase> phases = null;
	private final List<TargetPhase> excludePhases = new LinkedList<>();

	public TargetLifecycle getLifecycle() {
		return this.lifecycle;
	}

	public String getLifecycleId() {
		if (this.lifecycle == null)
			return null;
		return this.lifecycle.getId();
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
		if (this.mode != null || !this.executions.isEmpty() || this.lifecycle != null)
			return true;
		if (this.phases != null && !this.phases.isEmpty())
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

		if (this.mode == null) {
			if (fork.getMode() != null)
				return false;
		} else if (!this.mode.equals(fork.getMode()))
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
