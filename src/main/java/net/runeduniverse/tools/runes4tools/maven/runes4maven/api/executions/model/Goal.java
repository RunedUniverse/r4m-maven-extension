package net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.model;

import org.apache.maven.plugin.descriptor.MojoDescriptor;

//import net.runeduniverse.lib.utils.logging.logs.CompoundTree;

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

	//public void toRecord(CompoundTree record) {
	//	record.append("goal", this.id);
	//}
}
