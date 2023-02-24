package net.runeduniverse.tools.maven.r4m.api.pem;

import java.io.OutputStream;

import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

public interface ProjectExecutionModelWriter {

	public OutputStream writeModel(ProjectExecutionModel pem);

}
