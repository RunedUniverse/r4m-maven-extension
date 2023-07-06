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
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelPackagingParser;
import net.runeduniverse.tools.maven.r4m.scanner.api.MavenProjectScanner;

@Component(role = MavenProjectScanner.class, hint = PackagingProjectScanner.HINT)
public class PackagingProjectScanner implements MavenProjectScanner {

	public static final String HINT = "packaging";

	@Requirement(role = ProjectExecutionModelPackagingParser.class)
	private Map<String, ProjectExecutionModelPackagingParser> pemPackagingParser;

	@Override
	public int getPriority() {
		return -1000;
	}

	@Override
	public void scan(MavenSession mvnSession, Collection<Plugin> extPlugins, final Set<Plugin> unidentifiablePlugins,
			MavenProject mvnProject, ExecutionArchiveSlice projectSlice) throws Exception {
		for (ProjectExecutionModelPackagingParser parser : this.pemPackagingParser.values())
			projectSlice.register(parser.parse());
	}

}
