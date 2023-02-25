package net.runeduniverse.tools.maven.r4m.api.pem.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.Recordable;

public class ProjectExecutionModel implements Recordable {
	private String version;
	private Class<?> parserType = null;
	private String parserHint = null;
	private boolean effective = false;
	private Set<Execution> executions = new LinkedHashSet<>(0);

	public ProjectExecutionModel() {
		this.parserHint = null;
	}

	public ProjectExecutionModel(final Class<?> parserType, final String parserHint) {
		this.parserType = parserType;
		this.parserHint = parserHint;
	}

	public String getVersion() {
		return this.version;
	}

	public Class<?> getParserType() {
		return this.parserType;
	}

	public String getParserHint() {
		return this.parserHint;
	}

	public boolean isEffective() {
		return this.effective;
	}

	public Set<Execution> getExecutions() {
		return Collections.unmodifiableSet(this.executions);
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setParser(Class<?> type, String hint) {
		this.parserType = type;
		this.parserHint = hint;
	}

	public void setEffective(boolean value) {
		this.effective = value;
	}

	public void addExecution(Execution execution) {
		this.executions.add(execution);
	}

	public void addExecutions(Collection<Execution> executions) {
		this.executions.addAll(executions);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("ProjectExecutionModel");

		for (Recordable execution : executions)
			tree.append(execution.toRecord());

		return tree;
	}
}
