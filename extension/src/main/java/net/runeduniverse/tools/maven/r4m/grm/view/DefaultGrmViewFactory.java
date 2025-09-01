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
package net.runeduniverse.tools.maven.r4m.grm.view;

import org.apache.maven.lifecycle.DefaultLifecycles;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementViewFactory;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;
import net.runeduniverse.tools.maven.r4m.grm.view.api.GoalView;
import net.runeduniverse.tools.maven.r4m.grm.view.api.ProjectView;
import net.runeduniverse.tools.maven.r4m.grm.view.api.RuntimeView;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;

@Component(role = GoalRequirementViewFactory.class)
public class DefaultGrmViewFactory implements GoalRequirementViewFactory {

	@Requirement
	protected DefaultLifecycles defaultLifeCycles;

	public ProjectView createProjectView(final MavenProject mvnProject) {
		return new DefaultProjectView(mvnProject.getGroupId(), mvnProject.getArtifactId(), mvnProject.getPackaging());
	}

	public RuntimeView createRuntimeView(final ExecutionArchiveSelectorConfig cnf, final String phase) {
		final Lifecycle lifecycle = this.defaultLifeCycles.get(phase);
		return new DefaultRuntimeView(lifecycle == null ? null : lifecycle.getId(), phase, cnf.getModes(),
				cnf.getActiveProfiles(), cnf.getInactiveProfiles(), cnf.getProvidedProfiles(),
				cnf.getActiveExecutions());
	}

	public GoalView createGoalView(final MojoExecution mojoExecution) {
		return new DefaultGoalView(mojoExecution.getGroupId(), mojoExecution.getArtifactId(), mojoExecution.getGoal());
	}

	public EntityView createEntityView(final ProjectView projectData, final RuntimeView runtimeData,
			final GoalView goalData) {
		return new DefaultEntityView(projectData, runtimeData, goalData);
	}
}
