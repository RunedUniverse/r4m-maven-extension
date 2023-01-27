package net.runeduniverse.tools.maven.r4m.pem;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelector;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSlice;

@Component(role = ExecutionArchive.class, instantiationStrategy = "keep-alive")
public class Archive implements ExecutionArchive {
	private Map<MavenProject, ExecutionArchiveSlice> registry = new LinkedHashMap<>();

	@Requirement
	private MavenSession mvnSession;

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

	public ExecutionArchiveSlice getSlice(MavenProject mvnProject) {
		return this.registry.get(mvnProject);
	}

	public ExecutionArchiveSelector newSelection() {
		return new Selector(this);
	}
}