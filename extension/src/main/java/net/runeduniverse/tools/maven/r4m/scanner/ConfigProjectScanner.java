package net.runeduniverse.tools.maven.r4m.scanner;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelConfigParser;
import net.runeduniverse.tools.maven.r4m.scanner.api.MavenProjectScanner;

@Component(role = MavenProjectScanner.class, hint = ConfigProjectScanner.HINT)
public class ConfigProjectScanner implements MavenProjectScanner {

	public static final String HINT = "config";

	@Requirement(role = ProjectExecutionModelConfigParser.class)
	private Map<String, ProjectExecutionModelConfigParser> pemConfigParser;

	@Override
	public int getPriority() {
		return 1000;
	}

	@Override
	public void scan(MavenSession mvnSession, Collection<Plugin> extPlugins, final Set<Plugin> unidentifiablePlugins,
			MavenProject mvnProject, ExecutionArchiveSlice projectSlice) throws Exception {
		for (ProjectExecutionModelConfigParser parser : this.pemConfigParser.values())
			projectSlice.register(parser.parse(mvnProject));
	}

}
