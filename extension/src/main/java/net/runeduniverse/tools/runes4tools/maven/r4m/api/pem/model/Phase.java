package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model;

import java.util.List;

public class Phase {
	private String id;

	private List<Goal> goals;

	public String getId() {
		return this.id;
	}

	public List<Goal> getGoals() {
		return this.goals;
	}
}
