package net.runeduniverse.tools.maven.r4m.api.pem;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

public interface ProjectExecutionModelConfigParser {

	ProjectExecutionModel parse(MavenProject mvnProject) throws Exception;

}
