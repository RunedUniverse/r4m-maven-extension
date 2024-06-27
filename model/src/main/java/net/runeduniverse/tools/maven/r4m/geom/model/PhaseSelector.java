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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;

public class PhaseSelector implements Recordable {

	private Set<GoalOrderInfo> before = new LinkedHashSet<>();
	private Set<GoalOrderInfo> after = new LinkedHashSet<>();

	private String phaseId = null;
	private String lifecycleId = null;

	public String getPhaseId() {
		return this.phaseId;
	}

	public String getLifecycleId() {
		return this.lifecycleId;
	}

	public Set<GoalOrderInfo> getBefore() {
		return Collections.unmodifiableSet(this.before);
	}

	public Set<GoalOrderInfo> getAfter() {
		return Collections.unmodifiableSet(this.after);
	}

	public void addBefore(GoalOrderInfo info) {
		this.before.add(info);
	}

	public void addAfter(GoalOrderInfo info) {
		this.after.add(info);
	}

	public void addBefore(Collection<GoalOrderInfo> infos) {
		this.before.addAll(infos);
	}

	public void addAfter(Collection<GoalOrderInfo> infos) {
		this.after.addAll(infos);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("PhaseSelector");
		return tree;
	}

}
