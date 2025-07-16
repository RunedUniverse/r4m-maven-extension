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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;

public class ProjectExecutionModel implements Recordable {

	protected final Map<String, ModelOverride> overrides = new LinkedHashMap<>(0);
	protected final Set<Execution> executions = new LinkedHashSet<>(0);

	protected String version;
	protected ModelSource source = null;
	protected Class<?> parserType = null;
	protected String parserHint = null;
	protected boolean userDefined = false;

	public ProjectExecutionModel() {
		this.parserHint = null;
	}

	public String getVersion() {
		return this.version;
	}

	public ModelSource getModelSource() {
		return this.source;
	}

	public ModelSource computeModelSourceIfAbsent(final Supplier<ModelSource> supplier) {
		if (this.source == null) {
			if (supplier == null)
				return null;
			return this.source = supplier.get();
		}
		return this.source;
	}

	public Class<?> getParserType() {
		return this.parserType;
	}

	public String getParserHint() {
		return this.parserHint;
	}

	public boolean isEffective() {
		final ModelOverride override = this.overrides.get(DeclareSuperPemOverride.TYPE);
		if (override == null)
			return false;
		return override.isActive();
	}

	// set when pem.xml is found in the project folder
	public boolean isUserDefined() {
		return this.userDefined;
	}

	public Collection<ModelOverride> getOverrides() {
		return Collections.unmodifiableCollection(this.overrides.values());
	}

	public Map<String, ModelOverride> getOverridesAsMap() {
		return Collections.unmodifiableMap(this.overrides);
	}

	public Set<Execution> getExecutions() {
		return Collections.unmodifiableSet(this.executions);
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public void setModelSource(final ModelSource source) {
		this.source = source;
	}

	public void setParser(final Class<?> type, final String hint) {
		this.parserType = type;
		this.parserHint = hint;
	}

	public void setEffective(final Boolean value) {
		if (value == null) {
			this.overrides.remove(DeclareSuperPemOverride.TYPE);
			return;
		}
		final ModelOverride override = this.overrides.computeIfAbsent(DeclareSuperPemOverride.TYPE,
				t -> new DeclareSuperPemOverride(false));
		override.setActive(value);
	}

	public void setUserDefined(final boolean value) {
		this.userDefined = value;
	}

	public void addOverride(final ModelOverride override) {
		if (override == null)
			return;
		this.overrides.put(override.type(), override);
	}

	public void addOverrides(final Collection<ModelOverride> overrides) {
		if (overrides == null)
			return;
		for (ModelOverride override : overrides)
			addOverride(override);
	}

	public void addExecution(final Execution execution) {
		this.executions.add(execution);
	}

	public void addExecutions(final Collection<Execution> executions) {
		this.executions.addAll(executions);
	}

	public ProjectExecutionModel copy() {
		final ProjectExecutionModel pem = new ProjectExecutionModel();

		pem.setVersion(this.version);
		pem.setModelSource(this.source);
		pem.setParser(this.parserType, this.parserHint);
		pem.setUserDefined(this.userDefined);

		pem.addOverrides(this.overrides.values());
		pem.addExecutions(this.executions);

		return pem;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("ProjectExecutionModel");

		tree.append("version", this.version);
		tree.append(this.source.toRecord());
		tree.append("parserType", this.parserType == null ? "null" : this.parserType.getCanonicalName());
		tree.append("parserHint", this.parserHint);
		tree.append("userDefined", Boolean.toString(this.userDefined));

		for (Recordable override : this.overrides.values())
			tree.append(override.toRecord());
		for (Recordable execution : this.executions)
			tree.append(execution.toRecord());

		return tree;
	}
}
