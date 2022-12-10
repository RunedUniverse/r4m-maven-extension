package net.runeduniverse.tools.runes4tools.maven.r4m.errors;

import org.apache.maven.model.Plugin;

public class DescriptorParsingException extends Exception {
	private static final long serialVersionUID = 1L;

	public DescriptorParsingException(Plugin plugin, String descriptorLocation, Throwable e) {
		super(createMessage(plugin, descriptorLocation, e), e);
	}

	private static String createMessage(Plugin plugin, String descriptorLocation, Throwable e) {
		String message = "Failed to parse plugin descriptor";

		if (plugin != null) {
			message += " for " + plugin.getId();
		}

		if (descriptorLocation != null) {
			message += " (" + descriptorLocation + ")";
		}

		if (e != null) {
			message += ": " + e.getMessage();
		}

		return message;
	}
}
