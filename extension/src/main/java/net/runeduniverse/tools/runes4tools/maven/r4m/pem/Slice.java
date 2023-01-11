package net.runeduniverse.tools.runes4tools.maven.r4m.pem;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.ExecutionArchiveSlice;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.ExecutionFilter;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.ExecutionSource;

public class Slice implements ExecutionArchiveSlice {
	private final MavenProject mvnProject;
	private Slice parent;
	private String version;
	private Map<String, Map<ExecutionSource, Execution>> executions = new LinkedHashMap<>();

	public Slice(MavenProject mvnProject, String version, Slice parent) {
		this.mvnProject = mvnProject;
		this.version = version;
		this.parent = parent;
	}

	public MavenProject getMvnProject() {
		return this.mvnProject;
	}

	public String getVersion() {
		return this.version;
	}

	public Slice getParent() {
		return this.parent;
	}

	@Override
	public Set<Execution> getExecutions(ExecutionFilter filter) {
		Set<Execution> executions = new LinkedHashSet<>();
		for (Map<ExecutionSource, Execution> entry : this.executions.values())
			for (Execution execution : entry.values())
				if (filter.apply(execution))
					executions.add(execution);
		return executions;
	}
}
