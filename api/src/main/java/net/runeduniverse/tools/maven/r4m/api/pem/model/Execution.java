package net.runeduniverse.tools.maven.r4m.api.pem.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.Recordable;

public class Execution implements Recordable {
	private String id = null;
	private ExecutionSource source = null;

	private final Set<Trigger> trigger = new LinkedHashSet<>();
	private boolean activeAlways = false;
	private boolean activeDefault = false;
	private boolean activeNever = false;
	private final Set<String> packagingProcedures = new LinkedHashSet<>();

	private final Map<String, Lifecycle> lifecycles = new LinkedHashMap<>();

	public Execution() {
	}

	public Execution(final String id, final ExecutionSource source) {
		this.id = id;
		this.source = source;
	}

	public String getId() {
		return this.id;
	}

	public ExecutionSource getSource() {
		return this.source;
	}

	public Set<Trigger> getTrigger() {
		return this.trigger;
	}

	public boolean isAlwaysActive() {
		return this.activeAlways;
	}

	public boolean isDefaultActive() {
		return this.activeDefault;
	}

	public boolean isNeverActive() {
		return this.activeNever;
	}

	public Set<String> getPackagingProcedures() {
		return this.packagingProcedures;
	}

	public Lifecycle getLifecycle(String lifecycleId) {
		return this.lifecycles.get(lifecycleId);
	}

	public Map<String, Lifecycle> getLifecycles() {
		return this.lifecycles;
	}

	public void setAlwaysActive(boolean value) {
		this.activeAlways = value;
	}

	public void setDefaultActive(boolean value) {
		this.activeDefault = value;
	}

	public void setNeverActive(boolean value) {
		this.activeNever = value;
	}

	public void addTrigger(Trigger trigger) {
		this.trigger.add(trigger);
	}

	public void addPackagingProcedure(String value) {
		this.packagingProcedures.add(value);
	}

	public void addLifecycle(Lifecycle lifecycle) {
		this.lifecycles.put(lifecycle.getId(), lifecycle);
	}

	public void addLifecycles(Collection<Lifecycle> lifecycles) {
		for (Lifecycle lifecycle : lifecycles)
			this.lifecycles.put(lifecycle.getId(), lifecycle);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("Execution");

		tree.append("id", this.id);
		tree.append("source", this.source.key());

		CompoundTree triggerTree = new CompoundTree("Trigger");
		boolean triggerExist = false;

		if (this.activeAlways) {
			triggerTree.append("always-active", "true");
			triggerExist = true;
		}
		if (this.activeDefault) {
			triggerTree.append("default-active", "true");
			triggerExist = true;
		}
		if (this.activeNever) {
			triggerTree.append("never-active", "true");
			triggerExist = true;
		}
		if (!this.trigger.isEmpty()) {
			triggerExist = true;
			for (Recordable trigger : this.trigger)
				triggerTree.append(trigger.toRecord());
		}
		if (!this.packagingProcedures.isEmpty()) {
			triggerExist = true;
			for (String procedure : this.packagingProcedures)
				triggerTree.append("packaging procedure", procedure);
		}

		if (triggerExist)
			tree.append(triggerTree);

		for (Recordable lifecycle : this.lifecycles.values())
			tree.append(lifecycle.toRecord());

		return tree;
	}
}
