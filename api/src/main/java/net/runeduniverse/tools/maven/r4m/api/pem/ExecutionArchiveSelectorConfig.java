package net.runeduniverse.tools.maven.r4m.api.pem;

import java.util.Set;

import org.apache.maven.project.MavenProject;

public interface ExecutionArchiveSelectorConfig {
	public MavenProject getActiveProject();

	public String getPackagingProcedure();

	public String getActiveExecution();

	public Set<String> getActiveProfiles();

	public Set<String> getInactiveProfiles();

	public Set<String> getProvidedProfiles();

	public Set<String> getModes();

}
