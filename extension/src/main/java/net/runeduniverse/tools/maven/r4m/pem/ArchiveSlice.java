package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionFilter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

public class ArchiveSlice implements ExecutionArchiveSlice {
	private final MavenProject mvnProject;
	private ExecutionArchiveSlice parent;
	private String version;
	private final Map<String, Map<ExecutionSource, Set<Execution>>> executions = new LinkedHashMap<>();
	private final Map<Execution, ProjectExecutionModel> executionOrigins = new LinkedHashMap<>();

	public ArchiveSlice(MavenProject mvnProject, String version, ArchiveSlice parent) {
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
	public ProjectExecutionModel getModel(Execution execution) {
		return this.executionOrigins.get(execution);
	}

	@Override
	public Set<Execution> getExecutions() {
		return Collections.unmodifiableSet(this.executionOrigins.keySet());
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
	public Set<Execution> getEffectiveExecutions(ExecutionFilter filter) {
		Set<Execution> executions = new LinkedHashSet<>();
		for (Map<ExecutionSource, Set<Execution>> entry : this.executions.values())
			for (Set<Execution> execCol : entry.values())
				for (Execution execution : execCol) {
					if (this.executionOrigins.get(execution)
							.isEffective() && filter.apply(execution))
						executions.add(execution);
				}
		return executions;
	}

	@Override
	public void setParent(ExecutionArchiveSlice parent) {
		this.parent = parent;
	}

	@Override
	public void register(ProjectExecutionModel pem) {
		if (pem == null)
			return;

		for (Execution execution : pem.getExecutions()) {
			this.executionOrigins.put(execution, pem);

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

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("ArchiveSlice");

		tree.append("version", this.version);

		tree.append("project id", this.mvnProject.getId());

		if (this.parent != null)
			tree.append("parent project id", this.parent.getMvnProject()
					.getId());

		for (Map<ExecutionSource, Set<Execution>> valuesBySource : this.executions.values())
			for (Set<Execution> executions : valuesBySource.values())
				for (Execution execution : executions)
					tree.append(execution.toRecord());

		return tree;
	}
}
