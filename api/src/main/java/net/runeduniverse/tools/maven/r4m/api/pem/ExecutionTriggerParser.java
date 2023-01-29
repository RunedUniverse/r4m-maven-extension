package net.runeduniverse.tools.maven.r4m.api.pem;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.api.pem.model.Trigger;

public interface ExecutionTriggerParser {
	public Trigger parse(PlexusConfiguration node);
}
