package net.runeduniverse.tools.runes4tools.maven.r4m.pem;

import java.util.Collection;
import java.util.HashSet;
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
	private ExecutionArchiveSlice parent;
	private String version;
	private Map<String, Map<ExecutionSource, Set<Execution>>> executions = new LinkedHashMap<>();

	public Slice(MavenProject mvnProject, String version, Slice parent) {
		this.mvnProject = mvnProject;
		this.version = version;
		this.parent = parent;
	}

	@Override
	public MavenProject getMvnProject() {
		return this.mvnProject;
	}

	@Override
	public String getVersion() {
		return this.version;
	}

	@Override
	public ExecutionArchiveSlice getParent() {
		return this.parent;
	}

	@Override
	public Set<Execution> getExecutions(ExecutionFilter filter) {
		Set<Execution> executions = new LinkedHashSet<>();
		for (Map<ExecutionSource, Set<Execution>> entry : this.executions.values())
			for (Set<Execution> execCol : entry.values())
				for (Execution execution : execCol)
					if (filter.apply(execution))
						executions.add(execution);
		return executions;
	}

	@Override
	public void setParent(ExecutionArchiveSlice parent) {
		this.parent = parent;
	}

	@Override
	public void register(Collection<Execution> executions) {
		for (Execution execution : executions) {
			Map<ExecutionSource, Set<Execution>> entry = this.executions.get(execution.getId());
			if (entry == null) {
				entry = new LinkedHashMap<>(3);
				this.executions.put(execution.getId(), entry);
			}
			Set<Execution> col = entry.get(execution.getSource());
			if (col == null) {
				col = new HashSet<>();
				entry.put(execution.getSource(), col);
			}
			col.add(execution);
		}
	}
}
