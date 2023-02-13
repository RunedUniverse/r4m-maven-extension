package net.runeduniverse.tools.maven.r4m.api.pem.model;

public class TargetLifecycle {

	private String id;
	private String startPhase = null;
	private String stopPhase = null;

	public TargetLifecycle(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public String getStartPhase() {
		return this.startPhase;
	}

	public String getStopPhase() {
		return this.stopPhase;
	}

	public void setStartPhase(String value) {
		this.startPhase = value;
	}

	public void setStopPhase(String value) {
		this.stopPhase = value;
	}
}
