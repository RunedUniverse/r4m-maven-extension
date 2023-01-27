package net.runeduniverse.tools.maven.r4m.api.pem;

import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;

public interface ProjectExecutionModelConfigParser {

	Set<Execution> parse(MavenProject mvnProject);

}
