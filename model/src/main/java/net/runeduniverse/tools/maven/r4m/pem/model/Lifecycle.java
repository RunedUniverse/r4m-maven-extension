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
package net.runeduniverse.tools.maven.r4m.pem.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;

public class Lifecycle implements Recordable {

	private String id;
	private Map<String, Phase> phases = new LinkedHashMap<>();

	public Lifecycle() {
	}

	public Lifecycle(final String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public Phase getPhase(String phaseId) {
		return this.phases.get(phaseId);
	}

	public Map<String, Phase> getPhases() {
		return this.phases;
	}

	public void putPhase(Phase phase) {
		this.phases.put(phase.getId(), phase);
	}

	public void addPhases(List<Phase> phases) {
		for (Phase phase : phases)
			this.phases.put(phase.getId(), phase);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new DefaultCompoundTree("Lifecycle");

		tree.append("id", this.id);

		for (Recordable phase : this.phases.values())
			tree.append(phase.toRecord());

		return tree;
	}

}
