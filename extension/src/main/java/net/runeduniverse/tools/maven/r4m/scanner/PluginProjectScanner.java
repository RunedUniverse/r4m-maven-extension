package net.runeduniverse.tools.maven.r4m.scanner;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelPluginParser;
import net.runeduniverse.tools.maven.r4m.scanner.api.MavenProjectScanner;

@Component(role = MavenProjectScanner.class, hint = PluginProjectScanner.HINT)
public class PluginProjectScanner implements MavenProjectScanner {

	public static final String HINT = "plugin";

	@Requirement(role = ProjectExecutionModelPluginParser.class)
	private Map<String, ProjectExecutionModelPluginParser> pemPluginParser;

	@Override
	public int getPriority() {
		return 0;
	}

	private boolean isIdentifiable(final Set<Plugin> unidentifiablePlugins, Plugin mvnPlugin) {
		if (unidentifiablePlugins.contains(mvnPlugin))
			return false;

		if (mvnPlugin.getVersion() == null) {
			unidentifiablePlugins.add(mvnPlugin);
			return false;
		}

		return true;
	}

	@Override
	public void scan(MavenSession mvnSession, Collection<Plugin> extPlugins, final Set<Plugin> unidentifiablePlugins,
			MavenProject mvnProject, ExecutionArchiveSlice projectSlice) throws Exception {
		for (ProjectExecutionModelPluginParser parser : this.pemPluginParser.values())
			for (Plugin mvnPlugin : mvnProject.getBuildPlugins())
				if (isIdentifiable(unidentifiablePlugins, mvnPlugin))
					try {
						projectSlice.register(parser.parse(mvnProject.getRemotePluginRepositories(),
								mvnSession.getRepositorySession(), mvnPlugin));
					} catch (PluginResolutionException e) {
						unidentifiablePlugins.add(mvnPlugin);
					}
	}

}
