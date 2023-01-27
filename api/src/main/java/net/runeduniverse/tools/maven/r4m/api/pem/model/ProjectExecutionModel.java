package net.runeduniverse.tools.maven.r4m.api.pem.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProjectExecutionModel {
	private String version;
	private Set<Execution> executions = new LinkedHashSet<>(0);

	public String getVersion() {
		return this.version;
	}

	public Set<Execution> getExecutions() {
		return Collections.unmodifiableSet(this.executions);
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void addExecution(Execution execution) {
		this.executions.add(execution);
	}

	public void addExecutions(Collection<Execution> executions) {
		this.executions.addAll(executions);
	}
}
