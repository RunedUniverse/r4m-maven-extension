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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.indexer.AProjectBoundArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSector;

@Component(role = ExecutionArchive.class, instantiationStrategy = "keep-alive")
public class Archive extends AProjectBoundArchive<ArchiveSector, ExecutionArchiveSector> implements ExecutionArchive {

	private Map<MavenProject, ExecutionArchiveSector> registry = new LinkedHashMap<>();

	@Requirement
	private MavenSession mvnSession;
	@Requirement
	private MavenPluginManager pluginManager;

	@Override
	protected ArchiveSector newSector(MavenProject mvnProject) {
		return new ArchiveSector(mvnProject, null, null);
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new CompoundTree("ExecutionArchive");

		for (ExecutionArchiveSector slice : this.registry.values())
			tree.append(slice.toRecord());

		return tree;
	}
}
