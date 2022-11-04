package net.runeduniverse.tools.runes4tools.maven.runes4maven.errors;

import org.apache.maven.model.Plugin;

public class ExecutionDescriptorParsingException extends DescriptorParsingException {

	private static final long serialVersionUID = 6897715939965731517L;

	public ExecutionDescriptorParsingException(Plugin plugin, String descriptorLocation, Throwable e) {
		super(plugin, descriptorLocation, e);
	}
}
