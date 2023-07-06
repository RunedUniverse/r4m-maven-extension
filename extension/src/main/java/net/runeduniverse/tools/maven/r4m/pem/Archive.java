/*
 * Copyright © 2023 VenaNocta (venanocta@gmail.com)
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
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSlice;

@Component(role = ExecutionArchive.class, instantiationStrategy = "keep-alive")
public class Archive implements ExecutionArchive {

	private Map<MavenProject, ExecutionArchiveSlice> registry = new LinkedHashMap<>();

	@Requirement
	private MavenSession mvnSession;
	@Requirement
	private MavenPluginManager pluginManager;

	@Override
	public ExecutionArchiveSlice createSlice(MavenProject mvnProject) {
		ProjectDependencyGraph graph = this.mvnSession.getProjectDependencyGraph();
		ExecutionArchiveSlice slice = new ArchiveSlice(mvnProject, null, null);
		this.registry.put(mvnProject, slice);

		// this should contain max 1 project
		for (MavenProject upstreamMvnProject : graph.getUpstreamProjects(mvnProject, false))
			slice.setParent(this.registry.get(upstreamMvnProject));

		ExecutionArchiveSlice downstreamSlice = null;
		for (MavenProject downstreamMvnProject : graph.getDownstreamProjects(mvnProject, false)) {
			downstreamSlice = this.registry.get(downstreamMvnProject);
			if (downstreamSlice == null)
				continue;
			downstreamSlice.setParent(slice);
		}
		return slice;
	}

	@Override
	public ExecutionArchiveSlice getSlice(MavenProject mvnProject) {
		return this.registry.get(mvnProject);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("ExecutionArchive");

		for (ExecutionArchiveSlice slice : this.registry.values())
			tree.append(slice.toRecord());

		return tree;
	}

}
