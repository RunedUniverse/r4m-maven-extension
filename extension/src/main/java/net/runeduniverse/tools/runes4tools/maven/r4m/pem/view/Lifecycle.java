package net.runeduniverse.tools.runes4tools.maven.r4m.pem.view;

import java.util.LinkedHashMap;
import java.util.Map;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.view.LifecycleView;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.view.PhaseView;

public class Lifecycle implements LifecycleView {

	private String id;
	private Map<String, PhaseView> phases = new LinkedHashMap<>();

	public Lifecycle(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Map<String, PhaseView> getPhases() {
		return this.phases;
	}

	@Override
	public void put(PhaseView phaseView) {
		this.phases.put(phaseView.getId(), phaseView);
	}

	@Override
	public PhaseView getPhase(String phaseId) {
		return this.phases.get(phaseId);
	}

}