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
package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSectorSnapshot;
import net.runeduniverse.tools.maven.r4m.pem.api.ModelPredicate;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public class DefaultSectorSnapshot implements ExecutionArchiveSectorSnapshot {

	protected final Map<ProjectExecutionModel, Set<Execution>> models = new LinkedHashMap<>();
	protected final Map<String, Map<ExecutionSource, Set<Execution>>> executions = new LinkedHashMap<>();
	protected final Map<Execution, ProjectExecutionModel> executionOrigins = new LinkedHashMap<>();

	protected final MavenProject mvnProject;
	protected final ExecutionArchiveSectorSnapshot parent;

	public DefaultSectorSnapshot(final MavenProject mvnProject, final ExecutionArchiveSectorSnapshot parent) {
		this.mvnProject = mvnProject;
		this.parent = parent;
	}

	@Override
	public MavenProject getMvnProject() {
		return this.mvnProject;
	}

	@Override
	public ExecutionArchiveSectorSnapshot getParent() {
		return this.parent;
	}

	@Override
	public ProjectExecutionModel getModel(final Execution execution) {
		return this.executionOrigins.get(execution);
	}

	@Override
	public Map<String, AtomicBoolean> getOverridesAsBooleanMap() {
		final Map<String, AtomicBoolean> overrides = new LinkedHashMap<>(0);
		// check all except the user-defined pems
		for (ProjectExecutionModel pem : this.models.keySet()) {
			for (Entry<String, ModelOverride> entry : pem.getOverridesAsMap()
					.entrySet()) {
				if (pem == null || pem.isUserDefined())
					continue;
				final ModelOverride override = entry.getValue();
				if (override == null)
					continue;
				final AtomicBoolean active = overrides.computeIfAbsent(entry.getKey(), k -> new AtomicBoolean());
				if (active.get() || !override.isActive())
					continue;
				active.set(true);
			}
		}
		// check the user-defined pems
		for (ProjectExecutionModel pem : this.models.keySet()) {
			for (Entry<String, ModelOverride> entry : pem.getOverridesAsMap()
					.entrySet()) {
				if (pem == null || !pem.isUserDefined())
					continue;
				final ModelOverride override = entry.getValue();
				if (override == null)
					continue;
				final AtomicBoolean active = overrides.computeIfAbsent(entry.getKey(), k -> new AtomicBoolean());
				active.set(override.isActive());
			}
		}
		return overrides;
	}

	@Override
	public Map<String, AtomicBoolean> collectOverridesAsBooleanMap() {
		final Map<String, AtomicBoolean> overrides;
		if (this.parent == null)
			overrides = new LinkedHashMap<>(0);
		else
			overrides = this.parent.collectOverridesAsBooleanMap();

		mergeOverrides(overrides, getOverridesAsBooleanMap());
		return overrides;
	}

	@Override
	public Set<Execution> getExecutions() {
		return Collections.unmodifiableSet(this.executionOrigins.keySet());
	}

	@Override
	public void addModel(final MavenProject mvnProject, final ProjectExecutionModel pem) {
		if (mvnProject == null || pem == null)
			return;

		final Set<Execution> perModelSet = this.models.computeIfAbsent(pem, k -> new LinkedHashSet<>(1));
		for (Execution execution : pem.getExecutions()) {
			if (execution == null)
				continue;
			perModelSet.add(execution);
			this.executionOrigins.put(execution, pem);

			final Map<ExecutionSource, Set<Execution>> entry = this.executions.computeIfAbsent(execution.getId(),
					k -> new LinkedHashMap<>());
			final Set<Execution> col = entry.computeIfAbsent(execution.getSource(), k -> new HashSet<>());
			col.add(execution);
		}
	}

	protected void mergeOverrides(final Map<String, AtomicBoolean> base, final Map<String, AtomicBoolean> dominant) {
		for (Entry<String, AtomicBoolean> entry : dominant.entrySet()) {
			final AtomicBoolean value = base.computeIfAbsent(entry.getKey(), k -> new AtomicBoolean());
			final AtomicBoolean domValue = entry.getValue();
			value.set(domValue.get());
		}
	}

	@Override
	public ExecutionArchiveSectorSnapshot applyOverrides(
			final Function<Map<String, AtomicBoolean>, ModelPredicate<ProjectExecutionModel, Execution>>... filterSupplier) {
		final Map<String, AtomicBoolean> overrides = collectOverridesAsBooleanMap();

		if (filterSupplier == null)
			return this;

		for (Function<Map<String, AtomicBoolean>, ModelPredicate<ProjectExecutionModel, Execution>> supplier : filterSupplier) {
			applyFilter(supplier.apply(overrides));
		}

		return this;
	}

	@Override
	public ExecutionArchiveSectorSnapshot applyFilter(final ModelPredicate<ProjectExecutionModel, Execution> filter) {
		for (Map<ExecutionSource, Set<Execution>> entry : this.executions.values()) {
			for (Set<Execution> execCol : entry.values())
				for (Iterator<Execution> i = execCol.iterator(); i.hasNext();) {
					final Execution execution = (Execution) i.next();
					// apply filter & remove unmatched
					if (!filter.test(getModel(execution), execution)) {
						i.remove();
						final ProjectExecutionModel pem = this.executionOrigins.remove(execution);
						if (pem != null) {
							this.models.getOrDefault(pem, Collections.emptySet())
									.remove(execution);
						}
					}
				}
		}
		return this;
	}
}
