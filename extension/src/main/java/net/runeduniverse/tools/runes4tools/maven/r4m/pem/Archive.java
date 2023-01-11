package net.runeduniverse.tools.runes4tools.maven.r4m.pem;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.ExecutionArchive;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.ExecutionArchiveSlice;

@Component(role = ExecutionArchive.class, instantiationStrategy = "keep-alive")
public class Archive implements ExecutionArchive {
	private Map<MavenProject, Slice> registry = new LinkedHashMap<>();

	@Override
	public ExecutionArchiveSlice createSlice(MavenProject mvnProject) {
		
		// TODO Auto-generated method stub
		return null;
	}

	public ExecutionArchiveSlice getSlice(MavenProject mvnProject) {
		return this.registry.get(mvnProject);
	}

	public ExecutionArchiveSelection newSelection() {
		return new Selection(this);
	}
}
