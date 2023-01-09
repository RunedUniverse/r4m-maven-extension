package net.runeduniverse.tools.runes4tools.maven.r4m.pem;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.runes4tools.maven.r4m.Properties;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = Properties.EXECUTIONS_PARSER_LIFECYCLE_PARTICIPANT_HINT)
public class ProjectExecutionModelLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	@Requirement
	private Logger log;

	/**
	 * Invoked after all MavenProject instances have been created.
	 *
	 * This callback is intended to allow extensions to manipulate MavenProjects
	 * before they are sorted and actual build execution starts.
	 */
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {

		// TODO Parse/Collect Executions -> Archive
		
	}
}
