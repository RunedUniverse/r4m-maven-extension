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

import java.util.LinkedHashSet;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;

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

		if (!this.id.equals(phase.getId()) || !(this.executions.size() == phase.getExecutions()
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
