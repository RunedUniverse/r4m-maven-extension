package net.runeduniverse.tools.maven.r4m.pem.writer;

import java.io.OutputStream;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

@Component(role = ProjectExecutionModelWriter.class)
public class PEMWriter implements ProjectExecutionModelWriter {

	@Override
	public OutputStream writeModel(ProjectExecutionModel pem) {
		// TODO Auto-generated method stub
		return null;
	}

}
