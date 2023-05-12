package net.runeduniverse.tools.maven.r4m.pem.api;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public interface ProjectExecutionModelConfigParser {

	ProjectExecutionModel parse(MavenProject mvnProject) throws Exception;

}
