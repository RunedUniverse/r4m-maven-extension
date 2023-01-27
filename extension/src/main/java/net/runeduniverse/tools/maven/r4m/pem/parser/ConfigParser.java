package net.runeduniverse.tools.maven.r4m.pem.parser;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelConfigParser;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;

@Component(role = ProjectExecutionModelConfigParser.class, hint = "default")
public class ConfigParser implements ProjectExecutionModelConfigParser {

	@Override
	public Set<Execution> parse(MavenProject mvnProject) {
		// TODO Auto-generated method stub
		return new HashSet<>(0);
	}

}
