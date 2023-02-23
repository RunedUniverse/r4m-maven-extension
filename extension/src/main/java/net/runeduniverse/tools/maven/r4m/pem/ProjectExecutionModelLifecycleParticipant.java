package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Map;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelConfigParser;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelPackagingParser;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelPluginParser;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = Properties.EXECUTIONS_PARSER_LIFECYCLE_PARTICIPANT_HINT)
public class ProjectExecutionModelLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	public static final String ERR_FAILED_LOADING_MAVEN_EXTENSION_CLASSREALM = "Failed loading maven-extension ClassRealm";

	@Requirement
	private Logger log;
	@Requirement
	private ExecutionArchive archive;
	@Requirement(role = ProjectExecutionModelConfigParser.class)
	private Map<String, ProjectExecutionModelConfigParser> pemConfigParser;
	@Requirement(role = ProjectExecutionModelPluginParser.class)
	private Map<String, ProjectExecutionModelPluginParser> pemPluginParser;
	@Requirement(role = ProjectExecutionModelPackagingParser.class)
	private Map<String, ProjectExecutionModelPackagingParser> pemPackagingParser;
	@Requirement
	private PlexusContainer container;

	private boolean coreExtension = false;

	public void afterSessionStart(MavenSession mvnSession) throws MavenExecutionException {
		this.coreExtension = true;

		mvnSession.getSettings()
				.addPluginGroup(Properties.GROUP_ID);
	}

	/**
	 * Invoked after all MavenProject instances have been created.
	 *
	 * This callback is intended to allow extensions to manipulate MavenProjects
	 * before they are sorted and actual build execution starts.
	 */
	public void afterProjectsRead(MavenSession mvnSession) throws MavenExecutionException {
		ClassRealm currentRealm = (ClassRealm) Thread.currentThread()
				.getContextClassLoader();
		ClassWorld world = currentRealm.getWorld();

		try {
			if (this.coreExtension) {
				ClassRealm mavenExtRealm = this.container.getContainerRealm()
						.getWorld()
						.getRealm("maven.ext");
				Thread.currentThread()
						.setContextClassLoader(mavenExtRealm);
			} else {
				// we need to reinitiate the r4m-maven-extension realm because maven injects an
				// outdated version of the plexus-utils
				ClassRealm realm = world.getRealm("plexus.core")
						.createChildRealm("extension>net.runeduniverse.tools.maven.r4m:r4m-maven-extension");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.api.pem");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.api.pem.model");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.parser");
				realm.importFrom(currentRealm, "net.runeduniverse.tools.maven.r4m.pem.parser.trigger");
				realm.importFrom(currentRealm, "net.runeduniverse.lib.utils.logging.logs");
				Thread.currentThread()
						.setContextClassLoader(realm);
			}
			crawlModels(mvnSession);
		} catch (Exception e) {
			throw new MavenExecutionException(ERR_FAILED_LOADING_MAVEN_EXTENSION_CLASSREALM, e);
		} finally {
			Thread.currentThread()
					.setContextClassLoader(currentRealm);
		}
	}

	private void crawlModels(MavenSession mvnSession) throws Exception {
		for (MavenProject mvnProject : mvnSession.getAllProjects()) {
			ExecutionArchiveSlice projectSlice = this.archive.createSlice(mvnProject);

			for (ProjectExecutionModelConfigParser parser : this.pemConfigParser.values())
				projectSlice.register(parser.parse(mvnProject));

			for (ProjectExecutionModelPluginParser parser : this.pemPluginParser.values())
				for (Plugin mvnPlugin : mvnProject.getBuildPlugins())
					projectSlice.register(parser.parse(mvnProject.getRemotePluginRepositories(),
							mvnSession.getRepositorySession(), mvnPlugin));

			for (ProjectExecutionModelPackagingParser parser : this.pemPackagingParser.values())
				projectSlice.register(parser.parse());
		}
	}
}
