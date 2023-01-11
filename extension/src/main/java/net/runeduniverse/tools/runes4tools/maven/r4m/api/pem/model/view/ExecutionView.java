package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.view;

import java.util.Map;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.ExecutionSource;

public interface ExecutionView {
	public String getId();

	public ExecutionSource getSource();

	public Map<String, LifecycleView> getLifecycles();
}
