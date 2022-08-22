package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.plugin.descriptor.PluginDescriptor;

public class Plugin {

	private final String groupId;
	private final String artifactId;
	private final String prefix;

	private final Set<Execution> executions;

	private PluginDescriptor descriptor;

	public Plugin(final String groupId, final String artifactId, final String prefix) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.prefix = prefix;
		this.executions = new LinkedHashSet<>();
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

	public PluginDescriptor getDescriptor() {
		return this.descriptor;
	}

	public Set<Execution> getExecutions() {
		return this.executions;
	}

	public void setDescriptor(PluginDescriptor descriptor) {
		this.descriptor = descriptor;
	}
}
