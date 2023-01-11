package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model;

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

	public Map<String, Lifecycle> getLifecycles() {
		return this.lifecycles;
	}
}