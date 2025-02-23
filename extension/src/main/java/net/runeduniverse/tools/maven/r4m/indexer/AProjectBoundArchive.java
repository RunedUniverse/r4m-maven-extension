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

import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;
import net.runeduniverse.tools.maven.r4m.indexer.api.ProjectBoundRegistry;

public abstract class AProjectBoundArchive<S> extends AProjectBoundRegistry<S>
		implements ProjectBoundRegistry<S>, Recordable {

	@Requirement
	protected MavenSession mvnSession;

	protected AProjectBoundArchive() {
		super();
	}

	protected AProjectBoundArchive(final Map<MavenProject, S> registry) {
		super(registry);
	}

	@Override
	public S createSector(final MavenProject mvnProject) {
		final S sector = super.createSector(mvnProject);

		final ProjectDependencyGraph graph = this.mvnSession.getProjectDependencyGraph();
		// this should contain max 1 project
		for (MavenProject upstreamMvnProject : graph.getUpstreamProjects(mvnProject, false))
			_updateSector(sector, this.sectors.get(upstreamMvnProject));

		S downstreamSector = null;
		for (MavenProject downstreamMvnProject : graph.getDownstreamProjects(mvnProject, false)) {
			downstreamSector = this.sectors.get(downstreamMvnProject);
			if (downstreamSector == null)
				continue;
			_updateSector(downstreamSector, sector);
		}

		return sector;
	}

	protected void _updateSector(final S child, final S parent) {
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new CompoundTree(_getRecordTitle());

		for (S sector : this.sectors.values()) {
			if (sector instanceof Recordable)
				tree.append(((Recordable) sector).toRecord());
		}

		return tree;
	}

	protected String _getRecordTitle() {
		return getClass().getCanonicalName();
	}
}
