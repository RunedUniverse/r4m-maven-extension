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
package net.runeduniverse.tools.maven.r4m.indexer;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.indexer.api.ProjectBoundArchive;
import net.runeduniverse.tools.maven.r4m.indexer.api.ProjectBoundArchiveSector;

public abstract class AProjectBoundArchive<T extends S, S extends ProjectBoundArchiveSector<S>>
		extends AProjectBoundRegistry<T, S> implements ProjectBoundArchive<S> {

	@Requirement
	protected MavenSession mvnSession;

	@Override
	public T createSector(final MavenProject mvnProject) {
		final ProjectDependencyGraph graph = this.mvnSession.getProjectDependencyGraph();
		final T sector = createSector(mvnProject);

		// this should contain max 1 project
		for (MavenProject upstreamMvnProject : graph.getUpstreamProjects(mvnProject, false))
			sector.setParent(this.registry.get(upstreamMvnProject));

		S downstreamSlice = null;
		for (MavenProject downstreamMvnProject : graph.getDownstreamProjects(mvnProject, false)) {
			downstreamSlice = this.registry.get(downstreamMvnProject);
			if (downstreamSlice == null)
				continue;
			downstreamSlice.setParent(sector);
		}
		return sector;
	}
}
