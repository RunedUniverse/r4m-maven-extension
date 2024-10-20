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

import java.util.LinkedList;
import java.util.List;

import net.runeduniverse.lib.utils.conditions.DefaultConditionInfo;

public class GoalOrderCheck extends GoalInfoCheck {

	private final WhenCheck when = new WhenCheck();

	private ExecutionSource source = null;

	public GoalOrderCheck() {
		super();
		this.conditions.add(when);
	}

	public GoalOrderCheck(String groupId, String artifactId, String goalId) {
		super(groupId, artifactId, goalId);
		this.conditions.add(when);
	}

	public WhenCheck getWhen() {
		return this.when;
	}

	public ExecutionSource getSourceId() {
		return this.source;
	}

	public void setSource(ExecutionSource source) {
		this.source = source;
	}

	@Override
	public List<ConditionInfo> getInfo() {
		final List<ConditionInfo> lst = new LinkedList<>();
		lst.add(new DefaultConditionInfo("source", this.source.key()));
		return lst;
	}
}
