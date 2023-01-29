package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;

public class SelectorConfig implements ExecutionArchiveSelectorConfig {
	private MavenProject mvnProject = null;
	private String packagingProcedure = null;
	private String activeExecution = null;
	private final Set<String> activeProfiles = new LinkedHashSet<>();
	private final Set<String> providedProfiles = new LinkedHashSet<>();
	private final Set<String> modes = new LinkedHashSet<>();

	private final Set<String> inactiveProfiles = new LinkedHashSet<>();

	public void selectActiveProject(MavenProject value) {
		this.mvnProject = value;
	}

	// used to select for the packaging flag in pom. Keep in mind there are multiple
	// packaging flags that set the type of how an artifact is packaged. So here we
	// define it as packaging procedure.
	public void selectPackagingProcedure(String value) {
		this.packagingProcedure = value;
	}

	public void selectActiveExecution(String value) {
		this.activeExecution = value;
	}

	public void selectActiveProfiles(String... values) {
		for (int i = 0; i < values.length; i++)
			this.activeProfiles.add(values[i]);
	}

	public void selectActiveProfiles(Collection<String> values) {
		this.activeProfiles.addAll(values);
	}

	public void selectProvidedProfiles(String... values) {
		for (int i = 0; i < values.length; i++)
			this.providedProfiles.add(values[i]);
	}

	public void selectProvidedProfiles(Collection<String> values) {
		this.providedProfiles.addAll(values);
	}

	public void selectModes(String... values) {
		for (int i = 0; i < values.length; i++)
			this.modes.add(values[i]);
	}

	public void selectModes(Collection<String> values) {
		this.modes.addAll(values);
	}

	public void clearActiveProject() {
		this.mvnProject = null;
	}

	public void clearPackagingProcedure() {
		this.packagingProcedure = null;
	}

	public void clearActiveExecution() {
		this.activeExecution = null;
	}

	public void clearActiveProfiles() {
		this.activeProfiles.clear();
	}

	public void clearProvidedProfiles() {
		this.providedProfiles.clear();
	}

	public void clearModes() {
		this.modes.clear();
	}

	@Override
	public MavenProject getActiveProject() {
		return this.mvnProject;
	}

	@Override
	public String getPackagingProcedure() {
		return this.packagingProcedure;
	}

	@Override
	public String getActiveExecution() {
		return this.activeExecution;
	}

	@Override
	public Set<String> getActiveProfiles() {
		return Collections.unmodifiableSet(this.activeProfiles);
	}

	@Override
	public Set<String> getInactiveProfiles() {
		return Collections.unmodifiableSet(this.inactiveProfiles);
	}

	@Override
	public Set<String> getProvidedProfiles() {
		return Collections.unmodifiableSet(this.providedProfiles);
	}

	@Override
	public Set<String> getModes() {
		return Collections.unmodifiableSet(this.modes);
	}

	public SelectorConfig clone() {
		SelectorConfig cnf = new SelectorConfig();
		cnf.selectActiveProject(this.mvnProject);
		cnf.selectPackagingProcedure(this.packagingProcedure);
		cnf.selectActiveProfiles(Collections.unmodifiableSet(this.activeProfiles));
		cnf.selectProvidedProfiles(Collections.unmodifiableSet(this.providedProfiles));
		cnf.selectModes(Collections.unmodifiableSet(this.modes));
		return cnf;
	}

	public void compile(MavenSession mvnSession) {
		if (this.mvnProject == null)
			return;

		if (this.providedProfiles.isEmpty()) {
			this.providedProfiles.addAll(mvnSession.getSettings()
					.getProfilesAsMap()
					.keySet());

			for (MavenProject mvnProject : mvnSession.getProjectDependencyGraph()
					.getUpstreamProjects(this.mvnProject, true))
				for (Profile profile : mvnProject.getOriginalModel()
						.getProfiles())
					this.providedProfiles.add(profile.getId());

			for (Profile profile : this.mvnProject.getOriginalModel()
					.getProfiles())
				this.providedProfiles.add(profile.getId());
		}

		this.inactiveProfiles.clear();
		this.inactiveProfiles.addAll(this.providedProfiles);
		this.inactiveProfiles.removeAll(this.activeProfiles);
	}
}
