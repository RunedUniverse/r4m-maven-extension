package net.runeduniverse.tools.maven.r4m.pem;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelConfigParser;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelPackagingParser;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelPluginParser;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = Properties.EXECUTIONS_PARSER_LIFECYCLE_PARTICIPANT_HINT)
public class ProjectExecutionModelLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	@Requirement
	private Logger log;

	@Requirement
	private ExecutionArchive archive;

	@Requirement
	private Map<String, ProjectExecutionModelConfigParser> pemConfigParser;

	@Requirement
	private Map<String, ProjectExecutionModelPluginParser> pemPluginParser;

	@Requirement
	private Map<String, ProjectExecutionModelPackagingParser> pemPackagingParser;

	/**
	 * Invoked after all MavenProject instances have been created.
	 *
	 * This callback is intended to allow extensions to manipulate MavenProjects
	 * before they are sorted and actual build execution starts.
	 */
	public void afterProjectsRead(MavenSession mvnSession) throws MavenExecutionException {

		for (MavenProject mvnProject : mvnSession.getAllProjects()) {
			ExecutionArchiveSlice projectSlice = this.archive.createSlice(mvnProject);

			Set<Execution> configExec = new HashSet<>();
			for (ProjectExecutionModelConfigParser parser : this.pemConfigParser.values()) {
				configExec.addAll(parser.parse(mvnProject));
			}
			if (!configExec.isEmpty()) {
				// Project specific config overrides everything!
				projectSlice.register(configExec);
				continue;
			}

			for (ProjectExecutionModelPluginParser parser : this.pemPluginParser.values())
				for (Plugin mvnPlugin : mvnProject.getBuildPlugins()) {
					projectSlice.register(parser.parse(mvnProject.getRemotePluginRepositories(),
							mvnSession.getRepositorySession(), mvnPlugin));
				}

			for (ProjectExecutionModelPackagingParser parser : this.pemPackagingParser.values()) {
				projectSlice.register(parser.parse());
			}
		}

		this.log.debug(this.archive.toRecord()
				.toString());
	}
}
