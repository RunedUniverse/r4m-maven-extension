package net.runeduniverse.tools.runes4tools.maven.r4m.pem.model;

import java.util.Map;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.ExecutionSource;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.view.ExecutionView;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.view.LifecycleView;

public class EffectiveExecution implements ExecutionView {

	private String id;
	private ExecutionSource source;
	private Map<String, LifecycleView> lifecycleViews;

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public ExecutionSource getSource() {
		return this.source;
	}

	@Override
	public Map<String, LifecycleView> getLifecycles() {
		return this.lifecycleViews;
	}

}
