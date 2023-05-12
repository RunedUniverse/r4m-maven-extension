package net.runeduniverse.tools.maven.r4m.pem.api;

import java.io.OutputStream;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetPhase;

public interface ProjectExecutionModelWriter {

	public void writeModel(final OutputStream stream, final ProjectExecutionModel pem);

	public PlexusConfiguration convert(final ProjectExecutionModel pem);

	public PlexusConfiguration convert(final Execution execution);

	public PlexusConfiguration convert(final Lifecycle lifecycle);

	public PlexusConfiguration convert(final Phase phase);

	public PlexusConfiguration convert(final Goal goal);

	public PlexusConfiguration convert(final Fork fork);

	public PlexusConfiguration convert(final TargetLifecycle targetLifecycle);

	public PlexusConfiguration convert(final TargetPhase targetPhase);

}
