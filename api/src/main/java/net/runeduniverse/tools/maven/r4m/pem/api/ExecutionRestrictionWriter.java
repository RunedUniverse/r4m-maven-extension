package net.runeduniverse.tools.maven.r4m.pem.api;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;

public interface ExecutionRestrictionWriter {

	public boolean append(final PlexusConfiguration restrictionsNode, final ExecutionRestriction<?> restriction);

}
