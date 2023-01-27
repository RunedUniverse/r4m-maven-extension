package net.runeduniverse.tools.maven.r4m.api.pem;

import java.util.Collection;
import java.util.Set;

import org.apache.maven.project.MavenProject;

public interface ExecutionArchiveSelector extends Cloneable {
	public ExecutionArchiveSelector selectActiveProject(MavenProject value);

	/***
	 * used to select for the packaging flag in pom. Keep in mind there are multiple
	 * packaging flags that set the type of how an artifact is packaged. So here we
	 * define it as packaging procedure.
	 * 
	 * @param value packaging procedure
	 * @return selection
	 */
	public ExecutionArchiveSelector selectPackagingProcedure(String value);

	public ExecutionArchiveSelector selectActiveExecution(String value);

	public ExecutionArchiveSelector selectActiveProfiles(String... values);

	public ExecutionArchiveSelector selectActiveProfiles(Collection<String> values);

	public ExecutionArchiveSelector selectProvidedProfiles(String... values);

	public ExecutionArchiveSelector selectProvidedProfiles(Collection<String> values);

	public ExecutionArchiveSelector selectModes(String... values);

	public ExecutionArchiveSelector selectModes(Collection<String> values);

	public ExecutionArchiveSelector clearActiveProject();

	public ExecutionArchiveSelector clearPackagingProcedure();

	public ExecutionArchiveSelector clearActiveExecution();

	public ExecutionArchiveSelector clearActiveProfiles();

	public ExecutionArchiveSelector clearProvidedProfiles();

	public ExecutionArchiveSelector clearModes();

	public ExecutionArchive getArchive();

	public MavenProject getActiveProject();

	public String getPackagingProcedure();

	public String getActiveExecution();

	public Set<String> getActiveProfiles();

	public Set<String> getProvidedProfiles();

	public Set<String> getModes();

	public ExecutionArchiveSelection compile();

	public void copy(ExecutionArchiveSelector selection);
}
