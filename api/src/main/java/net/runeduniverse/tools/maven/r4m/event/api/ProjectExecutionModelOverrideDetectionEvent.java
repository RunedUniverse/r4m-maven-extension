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
package net.runeduniverse.tools.maven.r4m.event.api;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public interface ProjectExecutionModelOverrideDetectionEvent {

	public MavenProject getTopLevelMvnProject();

	public MavenProject getMvnProject();

	public Map<String, AtomicBoolean> getOverrides();

	public Set<ProjectExecutionModel> getModels();

	public static BasicEvent createEvent(final MavenProject topLevelMvnProject, final MavenProject mvnProject,
			final Map<String, AtomicBoolean> overrides, final Set<ProjectExecutionModel> models) {
		return new BasicEvent(topLevelMvnProject, mvnProject, overrides, models);
	}

	public class BasicEvent implements ProjectExecutionModelOverrideDetectionEvent {

		protected MavenProject topLevelMvnProject;
		protected MavenProject mvnProject;
		protected Map<String, AtomicBoolean> overrides;
		protected Set<ProjectExecutionModel> models;

		public BasicEvent(final MavenProject topLevelMvnProject, final MavenProject mvnProject,
				final Map<String, AtomicBoolean> overrides, final Set<ProjectExecutionModel> models) {
			this.topLevelMvnProject = topLevelMvnProject;
			this.mvnProject = mvnProject;
			this.overrides = overrides;
			this.models = models;
		}

		@Override
		public MavenProject getTopLevelMvnProject() {
			return this.topLevelMvnProject;
		}

		@Override
		public MavenProject getMvnProject() {
			return this.mvnProject;
		}

		@Override
		public Map<String, AtomicBoolean> getOverrides() {
			return this.overrides;
		}

		@Override
		public Set<ProjectExecutionModel> getModels() {
			return this.models;
		}

	}

}
