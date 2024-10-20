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
package net.runeduniverse.tools.maven.r4m.grm.data;

import org.apache.maven.lifecycle.DefaultLifecycles;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementDataFactory;
import net.runeduniverse.tools.maven.r4m.grm.model.data.EntityData;
import net.runeduniverse.tools.maven.r4m.grm.model.data.GoalData;
import net.runeduniverse.tools.maven.r4m.grm.model.data.ProjectData;
import net.runeduniverse.tools.maven.r4m.grm.model.data.RuntimeData;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;

@Component(role = GoalRequirementDataFactory.class)
public class DefaultGrmDataFactory implements GoalRequirementDataFactory{

	@Requirement
	protected DefaultLifecycles defaultLifeCycles;

	public ProjectData createProjectData(final MavenProject mvnProject) {
		return new DefaultProjectData(mvnProject.getGroupId(), mvnProject.getArtifactId(), mvnProject.getPackaging());
	}

	public RuntimeData createRuntimeData(final ExecutionArchiveSelectorConfig cnf, final String phase) {
		final Lifecycle lifecycle = this.defaultLifeCycles.get(phase);
		return new DefaultRuntimeData(lifecycle == null ? null : lifecycle.getId(), phase, cnf.getModes(),
				cnf.getActiveProfiles(), cnf.getInactiveProfiles(), cnf.getProvidedProfiles(),
				cnf.getActiveExecutions());
	}

	public GoalData createGoalData(final MojoExecution mojoExecution) {
		return new DefaultGoalData(mojoExecution.getGroupId(), mojoExecution.getArtifactId(), mojoExecution.getGoal());
	}

	public EntityData createEntityData(final ProjectData projectData, final RuntimeData runtimeData,
			final GoalData goalData) {
		return new DefaultEntityData(projectData, runtimeData, goalData);
	}
}
