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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;

public class Lifecycle implements DataEntry {

	public static final String HINT = "lifecycle";
	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle";

	protected final Supplier<Map<String, Phase>> phasesSupplier;

	protected final Map<String, Phase> phases;
	protected String id;

	public Lifecycle() {
		this(LinkedHashMap::new, null);
	}

	public Lifecycle(final String id) {
		this(LinkedHashMap::new, id);
	}

	public Lifecycle(final Supplier<Map<String, Phase>> phasesSupplier, final String id) {
		this.phasesSupplier = phasesSupplier;
		this.phases = this.phasesSupplier.get();
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public Phase getPhase(final String phaseId) {
		return this.phases.get(phaseId);
	}

	public Phase computePhaseIfAbsent(final String phaseId,
			final Function<? super String, ? extends Phase> mappingFunction) {
		return this.phases.computeIfAbsent(phaseId, mappingFunction);
	}

	public Map<String, Phase> getPhases() {
		return this.phases;
	}

	public void putPhase(final Phase phase) {
		this.phases.put(phase.getId(), phase);
	}

	public void putPhases(final Collection<Phase> phases) {
		for (Phase phase : phases)
			putPhase(phase);
	}

	@Override
	public Lifecycle copy() {
		final Lifecycle lifecycle = new Lifecycle(this.phasesSupplier, getId());

		lifecycle.putPhases(getPhases().values());

		return lifecycle;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("Lifecycle");

		tree.append("id", this.id);

		for (Recordable phase : this.phases.values())
			tree.append(phase.toRecord());

		return tree;
	}
}
