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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.common.api.DataMap;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSectorSnapshot;

public interface ProjectExecutionModelOverrideDetectionEvent {

	public MavenProject getTopLevelMvnProject();

	public MavenProject getMvnProject();

	public DataMap<String, AtomicBoolean, ExecutionArchiveSectorSnapshot.Data> getOverrides();

	public static BasicEvent createEvent(final MavenProject topLevelMvnProject, final MavenProject mvnProject,
			final DataMap<String, AtomicBoolean, ExecutionArchiveSectorSnapshot.Data> overrides) {
		return new BasicEvent(topLevelMvnProject, mvnProject, overrides);
	}

	public class BasicEvent implements ProjectExecutionModelOverrideDetectionEvent {

		protected MavenProject topLevelMvnProject;
		protected MavenProject mvnProject;
		protected DataMap<String, AtomicBoolean, ExecutionArchiveSectorSnapshot.Data> overrides;

		public BasicEvent(final MavenProject topLevelMvnProject, final MavenProject mvnProject,
				final DataMap<String, AtomicBoolean, ExecutionArchiveSectorSnapshot.Data> overrides) {
			this.topLevelMvnProject = topLevelMvnProject;
			this.mvnProject = mvnProject;
			this.overrides = overrides;
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
		public DataMap<String, AtomicBoolean, ExecutionArchiveSectorSnapshot.Data> getOverrides() {
			return this.overrides;
		}

	}

}
