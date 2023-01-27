package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Lifecycle {
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
}
