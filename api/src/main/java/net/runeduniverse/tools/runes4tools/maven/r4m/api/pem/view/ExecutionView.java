package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.view;

import java.util.Map;

public interface ExecutionView {
	public String getId();

	public Map<String, LifecycleView> getLifecycles();

	public LifecycleView getLifecycle(String lifecycleId);

	public void put(LifecycleView view);
}
