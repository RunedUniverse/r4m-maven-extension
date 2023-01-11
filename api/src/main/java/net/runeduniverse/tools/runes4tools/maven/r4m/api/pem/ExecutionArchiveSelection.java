package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem;

import java.util.Collection;
import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.view.ExecutionView;

public interface ExecutionArchiveSelection extends Cloneable {
	public ExecutionArchiveSelection selectActiveProject(MavenProject value);

	/***
	 * used to select for the packaging flag in pom. Keep in mind there are multiple
	 * packaging flags that set the type of how an artifact is packaged. So here we
	 * define it as packaging procedure.
	 * 
	 * @param value packaging procedure
	 * @return selection
	 */
	public ExecutionArchiveSelection selectPackagingProcedure(String value);

	public ExecutionArchiveSelection selectActiveExecution(String value);

	public ExecutionArchiveSelection selectActiveProfiles(String... values);

	public ExecutionArchiveSelection selectActiveProfiles(Collection<String> values);

	public ExecutionArchiveSelection selectProvidedProfiles(String... values);

	public ExecutionArchiveSelection selectProvidedProfiles(Collection<String> values);

	public ExecutionArchiveSelection selectModes(String... values);

	public ExecutionArchiveSelection selectModes(Collection<String> values);

	public ExecutionArchiveSelection clearActiveProject();

	public ExecutionArchiveSelection clearPackagingProcedure();

	public ExecutionArchiveSelection clearActiveExecution();

	public ExecutionArchiveSelection clearActiveProfiles();

	public ExecutionArchiveSelection clearProvidedProfiles();

	public ExecutionArchiveSelection clearModes();

	public ExecutionArchive getArchive();

	public MavenProject getActiveProject();

	public String getPackagingProcedure();

	public String getActiveExecution();

	public Set<String> getActiveProfiles();

	public Set<String> getProvidedProfiles();

	public Set<String> getModes();

	public Set<ExecutionView> getEffectiveExecutions();

	public void copy(ExecutionArchiveSelection selection);
}
