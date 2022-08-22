package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model;

import org.apache.maven.plugin.descriptor.MojoDescriptor;

public class Goal {

	private final String id;
	private MojoDescriptor descriptor;

	public Goal(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public MojoDescriptor getDescriptor() {
		return this.descriptor;
	}

	public void setDescriptor(MojoDescriptor descriptor) {
		this.descriptor = descriptor;
	}
}
