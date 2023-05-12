package net.runeduniverse.tools.maven.r4m.pem.view.api;

import java.util.Map;

import net.runeduniverse.lib.utils.logging.logs.Recordable;

public interface LifecycleView extends Recordable {

	public String getId();

	public Map<String, PhaseView> getPhases();

	public void put(PhaseView phaseView);

	public PhaseView getPhase(String id);

}
