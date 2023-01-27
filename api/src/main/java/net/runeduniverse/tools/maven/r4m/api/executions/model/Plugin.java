package net.runeduniverse.tools.maven.r4m.api.executions.model;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.maven.plugin.descriptor.PluginDescriptor;

public class Plugin {

	private final String groupId;
	private final String artifactId;

	private final Map<String, Execution> executions;

	private String prefix = null;
	private PluginDescriptor pluginDescriptor = null;
	private ExecutionDescriptor executionDescriptor = null;

	public Plugin(final String groupId, final String artifactId, final String prefix) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.prefix = prefix;
		this.executions = new LinkedHashMap<>();
	}

	public String getGroupId() {
		return this.groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public String getNamespacedKey() {
		return this.prefix == null ? this.groupId + ':' + this.artifactId : this.prefix;
	}

	public PluginDescriptor getDescriptor() {
		return this.pluginDescriptor;
	}

	public ExecutionDescriptor getExecutionDescriptor() {
		return this.executionDescriptor;
	}

	public Execution getExecution(String id) {
		return this.executions.get(id);
	}

	public Map<String, Execution> getExecutions() {
		return this.executions;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setDescriptor(PluginDescriptor descriptor) {
		this.pluginDescriptor = descriptor;
		this.prefix = this.pluginDescriptor.getGoalPrefix();
	}

	public void setExecutionDescriptor(ExecutionDescriptor descriptor) {
		this.executionDescriptor = descriptor;
		this.executions.clear();
		for (Execution item : this.executionDescriptor.getExecutions())
			this.executions.put(item.getId(), item);
		this.syncGoals();
	}

	public void putExecution(Execution execution) {
		this.executions.put(execution.getId(), execution);
	}

	public void syncGoals() {
		for (Execution exec : this.executions.values())
			for (Phase phase : exec.getPhases()
					.values())
				for (Goal goal : phase.getGoals())
					goal.setDescriptor(this.pluginDescriptor.getMojo(goal.getId()));
	}
}
