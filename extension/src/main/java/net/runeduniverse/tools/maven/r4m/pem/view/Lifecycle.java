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
package net.runeduniverse.tools.maven.r4m.pem.view;

import java.util.LinkedHashMap;
import java.util.Map;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;
import net.runeduniverse.tools.maven.r4m.pem.view.api.LifecycleView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.PhaseView;

public class Lifecycle implements LifecycleView {

	private String id;
	private Map<String, PhaseView> phases = new LinkedHashMap<>();

	public Lifecycle(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Map<String, PhaseView> getPhases() {
		return this.phases;
	}

	@Override
	public void put(PhaseView phaseView) {
		this.phases.put(phaseView.getId(), phaseView);
	}

	@Override
	public PhaseView getPhase(String phaseId) {
		return this.phases.get(phaseId);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("LifecycleView");

		tree.append("id", this.id);

		for (Recordable phase : this.phases.values())
			tree.append(phase.toRecord());

		return tree;
	}

}
