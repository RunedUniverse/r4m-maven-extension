package net.runeduniverse.tools.maven.r4m.api.pem.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Execution {
	private String id = null;
	private ExecutionSource source = null;

	private Set<ProfileTrigger> trigger = new LinkedHashSet<>();
	private boolean activeAlways = false;
	private boolean activeNever = false;
	private Set<String> packagingProcedures = new LinkedHashSet<>();

	private Map<String, Lifecycle> lifecycles = new LinkedHashMap<>();

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

	public Set<ProfileTrigger> getTrigger() {
		return this.trigger;
	}

	public boolean isAlwaysActive() {
		return this.activeAlways;
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
}
