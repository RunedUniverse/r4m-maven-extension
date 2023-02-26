package net.runeduniverse.tools.maven.r4m.api.pem;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionTrigger;

public interface ExecutionTriggerParser {
	public ExecutionTrigger parse(PlexusConfiguration node);
}
