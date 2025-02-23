/*
 * Copyright © 2024 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.tools.maven.r4m.pem;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;

public class SelectorConfig implements ExecutionArchiveSelectorConfig {

	private MavenProject mvnProject = null;
	private String packagingProcedure = null;
	private final Set<String> activeExecutions = new LinkedHashSet<>();
	private final Set<String> activeProfiles = new LinkedHashSet<>();
	private final Set<String> providedProfiles = new LinkedHashSet<>();
	private final Set<String> modes = new LinkedHashSet<>();

	private final Set<String> inactiveProfiles = new LinkedHashSet<>();
	private boolean dirty = true;

	@Override
	public ExecutionArchiveSelectorConfig selectActiveProject(MavenProject value) {
		this.dirty = true;
		this.mvnProject = value;
		return this;
	}

	// used to select for the packaging flag in pom. Keep in mind there are multiple
	// packaging flags that set the type of how an artifact is packaged. So here we
	// define it as packaging procedure.
	@Override
	public ExecutionArchiveSelectorConfig selectPackagingProcedure(String value) {
		this.dirty = true;
		this.packagingProcedure = value;
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig selectActiveExecutions(String... values) {
		this.dirty = true;
		for (String value : values)
			if (!isBlank(value))
				this.activeExecutions.add(value);
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig selectActiveExecutions(Collection<String> values) {
		this.dirty = true;
		this.activeExecutions.addAll(values);
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig selectActiveProfiles(String... values) {
		this.dirty = true;
		for (String value : values)
			if (!isBlank(value))
				this.activeProfiles.add(value);
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig selectActiveProfiles(Collection<String> values) {
		this.dirty = true;
		this.activeProfiles.addAll(values);
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig selectAllActiveProfiles(Collection<Profile> values) {
		this.dirty = true;
		for (Profile profile : values)
			this.activeProfiles.add(profile.getId());
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig selectProvidedProfiles(String... values) {
		this.dirty = true;
		for (String value : values)
			if (!isBlank(value))
				this.providedProfiles.add(value);
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig selectProvidedProfiles(Collection<String> values) {
		this.dirty = true;
		this.providedProfiles.addAll(values);
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig selectAllProvidedProfiles(Collection<Profile> values) {
		this.dirty = true;
		for (Profile profile : values)
			this.providedProfiles.add(profile.getId());
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig selectModes(String... values) {
		this.dirty = true;
		for (String value : values)
			if (!isBlank(value))
				this.modes.add(value);
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig selectModes(Collection<String> values) {
		this.dirty = true;
		this.modes.addAll(values);
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig clearActiveProject() {
		this.dirty = true;
		this.mvnProject = null;
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig clearPackagingProcedure() {
		this.dirty = true;
		this.packagingProcedure = null;
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig clearActiveExecution() {
		this.dirty = true;
		this.activeExecutions.clear();
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig clearActiveProfiles() {
		this.dirty = true;
		this.activeProfiles.clear();
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig clearProvidedProfiles() {
		this.dirty = true;
		this.providedProfiles.clear();
		return this;
	}

	@Override
	public ExecutionArchiveSelectorConfig clearModes() {
		this.dirty = true;
		this.modes.clear();
		return this;
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
	public Set<String> getActiveExecutions() {
		return Collections.unmodifiableSet(this.activeExecutions);
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

	@Override
	public SelectorConfig clone() {
		final SelectorConfig cnf = new SelectorConfig();
		cnf.selectActiveProject(this.mvnProject);
		cnf.selectPackagingProcedure(this.packagingProcedure);
		cnf.selectActiveProfiles(Collections.unmodifiableSet(this.activeProfiles));
		cnf.selectProvidedProfiles(Collections.unmodifiableSet(this.providedProfiles));
		cnf.selectModes(Collections.unmodifiableSet(this.modes));
		return cnf;
	}

	@Override
	public void compile(MavenSession mvnSession) {
		if (this.mvnProject == null)
			return;

		this.dirty = false;

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

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new CompoundTree("PEM Selector Config");

		if (this.dirty)
			tree.append("[WARNING]", "The SelectorConfig was not compiled prior to recording!");

		if (this.mvnProject != null)
			tree.append(new CompoundTree("Maven Project").append("groupId", this.mvnProject.getGroupId())
					.append("artifactId", this.mvnProject.getArtifactId())
					.append("version", this.mvnProject.getVersion()));

		if (this.packagingProcedure != null)
			tree.append("packaging procedure", this.packagingProcedure);
		if (!this.activeExecutions.isEmpty())
			tree.append("active executions", '[' + String.join(", ", this.activeExecutions) + ']');
		tree.append("modes", '[' + String.join(", ", this.modes) + ']');
		tree.append("active profiles", '[' + String.join(", ", this.activeProfiles) + ']');
		tree.append("provided profiles", '[' + String.join(", ", this.providedProfiles) + ']');
		tree.append("inactive profiles", '[' + String.join(", ", this.inactiveProfiles) + ']');

		return tree;
	}

}
