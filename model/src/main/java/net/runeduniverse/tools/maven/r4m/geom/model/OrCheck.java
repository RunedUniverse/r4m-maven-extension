/*
 * Copyright © 2024 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.geom.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;

public class OrCheck implements Check {

	protected Set<Check> checks = new LinkedHashSet<>();

	public OrCheck() {
	}

	public Set<Check> getChecks() {
		return this.checks;
	}

	public void addCheck(Check check) {
		this.checks.add(check);
	}

	public void addChecks(Collection<Check> checks) {
		this.checks.addAll(checks);
	}

	@Override
	public CompoundTree toRecord() {
		return toRecord(new CompoundTree("or"));
	}

	protected CompoundTree toRecord(CompoundTree tree) {
		for (Check check : this.checks) {
			tree.append(check.toRecord());
		}
		return tree;
	}

}
