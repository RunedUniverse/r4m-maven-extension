package net.runeduniverse.tools.maven.r4m.pem.writer;

import java.io.OutputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Phase;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.api.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.api.pem.model.TargetPhase;

@Component(role = ProjectExecutionModelWriter.class, hint = "default")
public class XMLWriter implements ProjectExecutionModelWriter {

	@Override
	public void writeModel(OutputStream stream, ProjectExecutionModel pem) {
		// TODO Auto-generated method stub

	}

	@Override
	public PlexusConfiguration convert(final ProjectExecutionModel pem) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlexusConfiguration convert(final Execution execution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlexusConfiguration convert(final Lifecycle lifecycle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlexusConfiguration convert(final Phase phase) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlexusConfiguration convert(final Goal goal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlexusConfiguration convert(final Fork fork) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlexusConfiguration convert(final TargetLifecycle targetLifecycle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlexusConfiguration convert(final TargetPhase targetPhase) {
		// TODO Auto-generated method stub
		return null;
	}

}
