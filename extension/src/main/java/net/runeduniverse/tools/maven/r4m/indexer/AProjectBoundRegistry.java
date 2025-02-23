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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.indexer.api.ProjectBoundRegistry;

public abstract class AProjectBoundRegistry<S> implements ProjectBoundRegistry<S> {

	protected final Map<MavenProject, S> sectors;

	@Requirement
	protected MavenSession mvnSession;

	protected AProjectBoundRegistry() {
		this.sectors = new LinkedHashMap<>();
	}

	protected AProjectBoundRegistry(final Map<MavenProject, S> registry) {
		this.sectors = registry;
	}

	protected abstract S _newSector(final MavenProject mvnProject);

	@Override
	public S createSector(final MavenProject mvnProject) {
		final S sector = _newSector(mvnProject);
		this.sectors.put(mvnProject, sector);
		return sector;
	}

	@Override
	public S getSector(final MavenProject mvnProject) {
		return this.sectors.get(mvnProject);
	}

	@Override
	public boolean hasSector(final MavenProject mvnProject) {
		return this.sectors.get(mvnProject) != null;
	}
}
