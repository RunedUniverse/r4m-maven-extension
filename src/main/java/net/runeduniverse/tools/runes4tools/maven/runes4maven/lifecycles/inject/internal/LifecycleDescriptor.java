package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal;

public class LifecycleDescriptor {
	private String prefix;
	private String lifecycleId;
	private String executionId;

	public LifecycleDescriptor() {
		this(null, null, null);
	}

	public LifecycleDescriptor(String prefix, String lifecycleId, String executionId) {
		this.prefix = prefix;
		this.lifecycleId = lifecycleId;
		this.executionId = executionId;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setLifecycleId(String lifecycleId) {
		this.lifecycleId = lifecycleId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public String getLifecycleId() {
		return this.lifecycleId;
	}

	public String getExecutionId() {
		return this.executionId;
	}

	public static LifecycleDescriptor fromTask(String task) {
		// task: runes4maven-inject#builder@default
		int len = task.length();
		int splitIdx = task.indexOf('#');

		String prefix = null;
		String lifecycleId = null;
		String executionId = null;
		if (0 < splitIdx)
			prefix = task.substring(0, splitIdx);
		if (++splitIdx < len)
			lifecycleId = task.substring(splitIdx);
		splitIdx = task.indexOf('@');
		if (0 < splitIdx)
			lifecycleId = lifecycleId.substring(0, splitIdx);
		if (++splitIdx < len)
			executionId = task.substring(splitIdx);
		return new LifecycleDescriptor(prefix, lifecycleId, executionId);
	}
}
