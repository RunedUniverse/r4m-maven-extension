package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class Phase {
	
	private final String id;

	private final Set<Goal> goals;

	public Phase(final String id) {
		this.id = id;
		this.goals = new LinkedHashSet<>();
	}
	
	public String getId() {
		return this.id;
	}

	public Set<Goal> getGoals() {
		return this.goals;
	}
}
