package net.runeduniverse.tools.maven.r4m.api.pem;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionRestriction;

public interface ExecutionRestrictionParser {
	public ExecutionRestriction parse(PlexusConfiguration node);
}
