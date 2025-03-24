/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.logging.log.api.Recordable;

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

	public ExecutionArchiveSelectorConfig selectAllActiveProfiles(Collection<Profile> values);

	public ExecutionArchiveSelectorConfig selectProvidedProfiles(String... values);

	public ExecutionArchiveSelectorConfig selectProvidedProfiles(Collection<String> values);

	public ExecutionArchiveSelectorConfig selectAllProvidedProfiles(Collection<Profile> values);

	public ExecutionArchiveSelectorConfig selectModes(String... values);

	public ExecutionArchiveSelectorConfig selectModes(Collection<String> values);

	public ExecutionArchiveSelectorConfig clearActiveProject();

	public ExecutionArchiveSelectorConfig clearProperties();

	public ExecutionArchiveSelectorConfig clearPackagingProcedure();

	public ExecutionArchiveSelectorConfig clearActiveExecution();

	public ExecutionArchiveSelectorConfig clearActiveProfiles();

	public ExecutionArchiveSelectorConfig clearProvidedProfiles();

	public ExecutionArchiveSelectorConfig clearModes();

	public MavenProject getActiveProject();

	public Properties getProperties();

	public String getPackagingProcedure();

	public Set<String> getActiveExecutions();

	public Set<String> getActiveProfiles();

	public Set<String> getInactiveProfiles();

	public Set<String> getProvidedProfiles();

	public Set<String> getModes();

	public ExecutionArchiveSelectorConfig clone();

	public void compile(MavenSession mvnSession);

}
