package net.runeduniverse.tools.maven.r4m.pem.api;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;

public interface ExecutionTriggerParser {

	public ExecutionTrigger<ExecutionArchiveSelectorConfig> parse(final PlexusConfiguration node);

}
