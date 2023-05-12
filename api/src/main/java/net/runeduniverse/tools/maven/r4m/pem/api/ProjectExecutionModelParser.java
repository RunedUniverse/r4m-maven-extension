package net.runeduniverse.tools.maven.r4m.pem.api;

import java.io.InputStream;

import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public interface ProjectExecutionModelParser {
	public void parseModel(ProjectExecutionModel pem, InputStream input) throws Exception;
}
