package net.runeduniverse.tools.maven.r4m.api.pem.view;

import java.util.Map;

import net.runeduniverse.tools.maven.r4m.api.pem.Recordable;

public interface LifecycleView extends Recordable{
	public String getId();

	public Map<String, PhaseView> getPhases();

	public void put(PhaseView phaseView);

	public PhaseView getPhase(String id);
}
