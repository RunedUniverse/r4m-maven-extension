package net.runeduniverse.tools.maven.r4m.pem.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.api.Recordable;

public class Execution implements Recordable {
	private String id = null;
	private ExecutionSource source = null;

	private final Set<ExecutionRestriction> restrictions = new LinkedHashSet<>();
	private final Set<ExecutionTrigger> trigger = new LinkedHashSet<>();
	private boolean inherited = true;
	private boolean activeAlways = false;
	private boolean activeDefault = false;
	private boolean activeNever = false;

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

	public Set<ExecutionTrigger> getTrigger() {
		return this.trigger;
	}

	public boolean isInherited() {
		return this.inherited;
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

	public Set<ExecutionRestriction> getRestrictions() {
		return this.restrictions;
	}

	public Lifecycle getLifecycle(String lifecycleId) {
		return this.lifecycles.get(lifecycleId);
	}

	public Map<String, Lifecycle> getLifecycles() {
		return this.lifecycles;
	}

	public void setInherited(boolean value) {
		this.inherited = value;
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

	public void addTrigger(ExecutionTrigger trigger) {
		this.trigger.add(trigger);
	}

	public void addRestriction(ExecutionRestriction value) {
		for (ExecutionRestriction restriction : this.restrictions)
			if (restriction != null && restriction.equals(value))
				return;
		this.restrictions.add(value);
	}

	public void addRestrictions(Collection<ExecutionRestriction> values) {
		for (ExecutionRestriction value : values)
			addRestriction(value);
	}

	public void putLifecycle(Lifecycle lifecycle) {
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
		tree.append("inherited", Boolean.toString(this.inherited));

		CompoundTree restrictionsTree = new CompoundTree("Restrictions");
		boolean restrictionsExist = false;

		if (!this.restrictions.isEmpty()) {
			restrictionsExist = true;
			for (Recordable restriction : this.restrictions)
				restrictionsTree.append(restriction.toRecord());
		}

		if (restrictionsExist)
			tree.append(restrictionsTree);

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

		if (triggerExist)
			tree.append(triggerTree);

		for (Recordable lifecycle : this.lifecycles.values())
			tree.append(lifecycle.toRecord());

		return tree;
	}
}