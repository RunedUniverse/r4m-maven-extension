package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model;

import java.util.Map;

public class Lifecycle {
	private String id;

	private Map<String, Phase> phases;

	public String getId() {
		return this.id;
	}

	public Map<String, Phase> getPhases() {
		return this.phases;
	}
}
