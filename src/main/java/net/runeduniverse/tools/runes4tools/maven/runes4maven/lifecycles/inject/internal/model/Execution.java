package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class Execution {

	private final String id;

	private final Set<Phase> phases;

	public Execution(final String id) {
		this.id = id;
		this.phases = new LinkedHashSet<>();
	}

	public String getId() {
		return this.id;
	}

	public Set<Phase> getPhases() {
		return this.phases;
	}
}
