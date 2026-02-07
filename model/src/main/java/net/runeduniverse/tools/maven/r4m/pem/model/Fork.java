/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
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
import java.util.function.Supplier;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;

import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;
import static net.runeduniverse.lib.utils.common.HashUtils.hash;

public class Fork implements DataEntry {

	public static final String HINT = "fork";
	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.pem.model.Fork";

	protected final Supplier<Set<String>> executionsSupplier;
	protected final Supplier<Set<TargetPhase>> excludePhasesSupplier;
	protected final Supplier<List<TargetPhase>> phasesSupplier;

	protected final Set<String> executions;
	protected final Set<TargetPhase> excludePhases;
	protected final List<TargetPhase> phases;

	protected String mode = null;
	protected TargetLifecycle lifecycle = null;

	public Fork() {
		this(() -> new LinkedHashSet<>(0), () -> new LinkedHashSet<>(0), LinkedList::new);
	}

	public Fork(final Supplier<Set<String>> executionsSupplier, final Supplier<Set<TargetPhase>> excludePhasesSupplier,
			final Supplier<List<TargetPhase>> phasesSupplier) {
		this.executionsSupplier = executionsSupplier;
		this.excludePhasesSupplier = excludePhasesSupplier;
		this.phasesSupplier = phasesSupplier;
		this.executions = this.executionsSupplier.get();
		this.excludePhases = this.excludePhasesSupplier.get();
		this.phases = this.phasesSupplier.get();
	}

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

	public Set<TargetPhase> getExcludedPhases() {
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

	public void setLifecycle(final TargetLifecycle lifecycle) {
		this.lifecycle = lifecycle;
	}

	public void setMode(final String value) {
		this.mode = value;
	}

	public void addExecution(final String execution) {
		this.executions.add(execution);
	}

	public void addExecutions(final Collection<String> executions) {
		this.executions.addAll(executions);
	}

	public void clearExecutions() {
		this.executions.clear();
	}

	public void addPhase(final TargetPhase phase) {
		this.phases.add(phase);
	}

	public void addPhase2(final String phase) {
		addPhase(new TargetPhase(phase));
	}

	public void addPhases(final List<TargetPhase> phases) {
		this.phases.addAll(phases);
	}

	public void addPhases2(final List<String> phases) {
		for (String phase : phases)
			addPhase2(phase);
	}

	public void clearPhases() {
		this.phases.clear();
	}

	public void addExcludedPhase(final TargetPhase excludedPhase) {
		this.excludePhases.add(excludedPhase);
	}

	public void addExcludedPhase2(final String excludedPhase) {
		addExcludedPhase(new TargetPhase(excludedPhase));
	}

	public void addExcludedPhases(final Collection<TargetPhase> excludedPhases) {
		this.excludePhases.addAll(excludedPhases);
	}

	public void addExcludedPhases2(final Collection<String> excludedPhases) {
		for (String phase : excludedPhases)
			addExcludedPhase2(phase);
	}

	public void clearExcludedPhases() {
		this.excludePhases.clear();
	}

	@Override
	public int hashCode() {
		return hash(type());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Fork))
			return false;
		final Fork fork = (Fork) obj;

		// Lists check using equals & also check the order of elements
		return objectEquals(this.mode, fork.getMode()) //
				&& objectEquals(this.executions, fork.getExecutions()) //
				&& objectEquals(this.lifecycle, fork.getLifecycle()) //
				&& objectEquals(this.phases, fork.getPhases()) //
				&& objectEquals(this.excludePhases, fork.getExcludedPhases());
	}

	@Override
	public Fork copy() {
		final Fork fork = new Fork(this.executionsSupplier, this.excludePhasesSupplier, this.phasesSupplier);

		fork.setLifecycle(getLifecycle().copy());
		fork.setMode(getMode());
		fork.addExecutions(getExecutions());
		for (TargetPhase phase : getPhases())
			fork.addPhase(phase.copy());
		for (TargetPhase phase : getExcludedPhases())
			fork.addExcludedPhase(phase.copy());

		return fork;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("Fork");

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
			final CompoundTree phasesTree = new DefaultCompoundTree("phases");
			for (TargetPhase phase : this.phases)
				phasesTree.append(phase.toRecord());
			tree.append(phasesTree);
		}

		if (!this.excludePhases.isEmpty()) {
			final CompoundTree phasesTree = new DefaultCompoundTree("excluded phases");
			for (TargetPhase phase : this.excludePhases)
				phasesTree.append(phase.toRecord());
			tree.append(phasesTree);
		}

		return tree;
	}
}
