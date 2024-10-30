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

import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.indexer.api.ProjectBoundRegistry;

public abstract class AProjectBoundRegistry<T extends S, S> implements ProjectBoundRegistry<S> {

	protected final Map<MavenProject, T> registry = new LinkedHashMap<>();
	@Requirement
	protected MavenPluginManager pluginManager;

	protected abstract T newSector(final MavenProject mvnProject);

	@Override
	public T createSector(final MavenProject mvnProject) {
		final T sector = newSector(mvnProject);
		this.registry.put(mvnProject, sector);
		return sector;
	}

	@Override
	public boolean hasSector(final MavenProject mvnProject) {
		return this.registry.containsKey(mvnProject);
	}

	@Override
	public T getSector(final MavenProject mvnProject) {
		return this.registry.get(mvnProject);
	}
}
