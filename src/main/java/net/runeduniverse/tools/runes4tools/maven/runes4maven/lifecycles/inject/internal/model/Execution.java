package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Execution {

	private final String id;

	private final Map<String, Phase> phases;

	public Execution(final String id) {
		this.id = id;
		this.phases = new LinkedHashMap<>();
	}

	public String getId() {
		return this.id;
	}
	
	public Phase getPhase(String id) {
		return this.phases.get(id);
	}

	public Map<String, Phase> getPhases() {
		return this.phases;
	}
	
	public void putPhase(Phase phase) {
		this.phases.put(phase.getId(), phase);
	}
}
