package net.runeduniverse.tools.maven.r4m.pem.view.api;

import java.util.Map;

import net.runeduniverse.lib.utils.logging.logs.Recordable;

public interface ExecutionView extends Recordable {

	public String getId();

	public Map<String, LifecycleView> getLifecycles();

	public LifecycleView getLifecycle(String lifecycleId);

	public void put(LifecycleView view);

}
