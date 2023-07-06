package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.Collection;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.logging.logs.Recordable;

public interface ExecutionArchiveSelectorConfig extends Recordable, Cloneable {

	public ExecutionArchiveSelectorConfig selectActiveProject(MavenProject value);

	/***
	 * used to select for the packaging flag in pom. Keep in mind there are multiple
	 * packaging flags that set the type of how an artifact is packaged. So here we
	 * define it as packaging procedure.
	 *
	 * @param value packaging procedure
	 * @return selection
	 */
	public ExecutionArchiveSelectorConfig selectPackagingProcedure(String value);

	public ExecutionArchiveSelectorConfig selectActiveExecutions(String... values);

	public ExecutionArchiveSelectorConfig selectActiveExecutions(Collection<String> values);

	public ExecutionArchiveSelectorConfig selectActiveProfiles(String... values);

	public ExecutionArchiveSelectorConfig selectActiveProfiles(Collection<String> values);

	public ExecutionArchiveSelectorConfig selectProvidedProfiles(String... values);

	public ExecutionArchiveSelectorConfig selectProvidedProfiles(Collection<String> values);

	public ExecutionArchiveSelectorConfig selectModes(String... values);

	public ExecutionArchiveSelectorConfig selectModes(Collection<String> values);

	public ExecutionArchiveSelectorConfig clearActiveProject();

	public ExecutionArchiveSelectorConfig clearPackagingProcedure();

	public ExecutionArchiveSelectorConfig clearActiveExecution();

	public ExecutionArchiveSelectorConfig clearActiveProfiles();

	public ExecutionArchiveSelectorConfig clearProvidedProfiles();

	public ExecutionArchiveSelectorConfig clearModes();

	public MavenProject getActiveProject();

	public String getPackagingProcedure();

	public Set<String> getActiveExecutions();

	public Set<String> getActiveProfiles();

	public Set<String> getInactiveProfiles();

	public Set<String> getProvidedProfiles();

	public Set<String> getModes();

	public ExecutionArchiveSelectorConfig clone();

	public void compile(MavenSession mvnSession);

}
