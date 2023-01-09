package net.runeduniverse.tools.runes4tools.maven.r4m.executions;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.runes4tools.maven.r4m.Properties;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = Properties.EXECUTIONS_PARSER_LIFECYCLE_PARTICIPANT_HINT)
public class ExecutionsParserMavenLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	@Requirement
	private Logger log;

	/**
	 * Invoked after MavenSession instance has been created.
	 *
	 * This callback is intended to allow extensions to inject execution properties,
	 * activate profiles and perform similar tasks that affect MavenProject instance
	 * construction.
	 */
	// TODO This is too early for build extensions, so maybe just remove it?
	public void afterSessionStart(MavenSession session) throws MavenExecutionException {
		// do nothing
		// NEVER GETS CALLED!!!
	}

	/**
	 * Invoked after all MavenProject instances have been created.
	 *
	 * This callback is intended to allow extensions to manipulate MavenProjects
	 * before they are sorted and actual build execution starts.
	 */
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {

		// TODO Parse/Collect Executions -> Archive

	}

	/**
	 * Invoked after all projects were built.
	 *
	 * This callback is intended to allow extensions to perform cleanup of any
	 * allocated external resources after the build. It is invoked on best-effort
	 * basis and may be missed due to an Error or RuntimeException in Maven core
	 * code.
	 * 
	 * @since 3.2.1, MNG-5389
	 */
	public void afterSessionEnd(MavenSession session) throws MavenExecutionException {
		// do nothing
		// NEVER GETS CALLED!!!
	}
}
