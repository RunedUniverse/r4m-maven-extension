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
package net.runeduniverse.tools.maven.r4m.grm.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;

public class GoalExecutionOrderModel implements Recordable {

	private final Set<GoalSelector> goals = new LinkedHashSet<>(0);

	private String version = null;
	private Class<?> parserType = null;
	private String parserHint = null;
	private boolean effective = false;

	public GoalExecutionOrderModel() {
		this.parserHint = null;
	}

	public GoalExecutionOrderModel(final Class<?> parserType, final String parserHint) {
		this.parserType = parserType;
		this.parserHint = parserHint;
	}

	public String getVersion() {
		return this.version;
	}

	public Class<?> getParserType() {
		return this.parserType;
	}

	public String getParserHint() {
		return this.parserHint;
	}

	public boolean isEffective() {
		return this.effective;
	}

	public Set<GoalSelector> getGoalSelector() {
		return Collections.unmodifiableSet(this.goals);
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setParser(Class<?> type, String hint) {
		this.parserType = type;
		this.parserHint = hint;
	}

	public void setEffective(boolean value) {
		this.effective = value;
	}

	public void addGoalSelector(GoalSelector selector) {
		this.goals.add(selector);
	}

	public void addGoalSelector(Collection<GoalSelector> selector) {
		this.goals.addAll(selector);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("GoalExecutionOrderModel");

		tree.append("effective", "" + this.effective);

		for (Recordable selector : this.goals)
			tree.append(selector.toRecord());

		return tree;
	}

}
