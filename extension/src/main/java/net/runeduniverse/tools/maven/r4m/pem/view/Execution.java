package net.runeduniverse.tools.maven.r4m.pem.view;

import java.util.LinkedHashMap;
import java.util.Map;
import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.Recordable;
import net.runeduniverse.tools.maven.r4m.api.pem.view.ExecutionView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.LifecycleView;

public class Execution implements ExecutionView {

	private String id;
	private Map<String, LifecycleView> lifecycles = new LinkedHashMap<>();

	public Execution(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Map<String, LifecycleView> getLifecycles() {
		return this.lifecycles;
	}

	@Override
	public void put(LifecycleView view) {
		this.lifecycles.put(view.getId(), view);
	}

	@Override
	public LifecycleView getLifecycle(String lifecycleId) {
		return this.lifecycles.get(lifecycleId);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("ExecutionView");

		tree.append("id", this.id);

		for (Recordable lifecycle : this.lifecycles.values())
			tree.append(lifecycle.toRecord());

		return tree;
	}

}
