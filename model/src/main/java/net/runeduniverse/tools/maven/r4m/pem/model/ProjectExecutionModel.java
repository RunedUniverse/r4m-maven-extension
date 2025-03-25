/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.pem.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;

public class ProjectExecutionModel implements Recordable {

	protected final Set<Execution> executions = new LinkedHashSet<>(0);

	protected String version;
	protected Class<?> parserType = null;
	protected String parserHint = null;
	protected boolean effective = false;

	public ProjectExecutionModel() {
		this.parserHint = null;
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

	// set when pem.xml is found in the project folder
	public boolean isEffective() {
		return this.effective;
	}

	public Set<Execution> getExecutions() {
		return Collections.unmodifiableSet(this.executions);
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public void setParser(final Class<?> type, final String hint) {
		this.parserType = type;
		this.parserHint = hint;
	}

	public void setEffective(final boolean value) {
		this.effective = value;
	}

	public void addExecution(final Execution execution) {
		this.executions.add(execution);
	}

	public void addExecutions(final Collection<Execution> executions) {
		this.executions.addAll(executions);
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("ProjectExecutionModel");

		for (Recordable execution : executions)
			tree.append(execution.toRecord());

		return tree;
	}

}
