/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;

import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;
import static net.runeduniverse.lib.utils.common.HashUtils.hash;

public class TargetPhase implements DataEntry {

	protected final Supplier<Set<String>> executionsSupplier;
	protected final Set<String> executions;
	protected final String id;

	public TargetPhase(final String id) {
		this(() -> new LinkedHashSet<>(0), id);
	}

	public TargetPhase(final Supplier<Set<String>> executionsSupplier, final String id) {
		this.executionsSupplier = executionsSupplier;
		this.executions = this.executionsSupplier.get();
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public Set<String> getExecutions() {
		return this.executions;
	}

	public void addExecution(final String execution) {
		this.executions.add(execution);
	}

	public void addExecutions(final Set<String> executions) {
		this.executions.addAll(executions);
	}

	@Override
	public int hashCode() {
		return hash(type()) ^ hash(getId());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof TargetPhase))
			return false;
		final TargetPhase phase = (TargetPhase) obj;

		return objectEquals(this.id, phase.getId()) //
				&& objectEquals(this.executions, phase.getExecutions());
	}

	@Override
	public TargetPhase copy() {
		final TargetPhase phase = new TargetPhase(this.executionsSupplier, getId());

		phase.addExecutions(getExecutions());

		return phase;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("Target Phase");

		tree.append("id", this.id);

		if (!this.executions.isEmpty())
			tree.append("executions", '[' + String.join(", ", this.executions) + ']');

		return tree;
	}
}
