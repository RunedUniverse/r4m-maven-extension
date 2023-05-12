package net.runeduniverse.tools.maven.r4m.pem.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.api.Recordable;

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
		CompoundTree tree = new CompoundTree("Lifecycle");

		tree.append("id", this.id);

		for (Recordable phase : this.phases.values())
			tree.append(phase.toRecord());

		return tree;
	}
}
