package net.runeduniverse.tools.maven.r4m.lifecycles.inject;

import net.runeduniverse.tools.maven.r4m.Properties;

public class LifecycleDescriptor {
	private String prefix;
	private String lifecycleId;
	private String executionId;

	public LifecycleDescriptor() {
		this(null, Properties.DEFAULT_LIFECYCLE_ID, Properties.DEFAULT_EXECUTION_ID);
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
		// task: r4m-inject#builder@default
		String prefix = null;
		String lifecycleId = null;
		String executionId = null;

		int len = task.length();
		int splitLifecycleIdx = task.indexOf('#');
		if (0 < splitLifecycleIdx)
			prefix = task.substring(0, splitLifecycleIdx);
		if (++splitLifecycleIdx < len)
			lifecycleId = task.substring(splitLifecycleIdx);
		int splitExecutionIdx = task.indexOf('@');
		if (0 < splitExecutionIdx)
			lifecycleId = lifecycleId.substring(0, splitExecutionIdx - splitLifecycleIdx);
		if (++splitExecutionIdx < len)
			executionId = task.substring(splitExecutionIdx);

		return new LifecycleDescriptor(prefix, lifecycleId, executionId);
	}
}
