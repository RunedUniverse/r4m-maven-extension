package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.view;

import java.util.Map;

public interface LifecycleView {
	public String getId();

	public Map<String, PhaseView> getPhases();

	public void put(PhaseView phaseView);

	public PhaseView getPhase(String id);
}
