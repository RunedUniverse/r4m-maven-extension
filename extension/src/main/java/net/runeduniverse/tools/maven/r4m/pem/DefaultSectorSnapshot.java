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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.common.LinkedHashDataMap;
import net.runeduniverse.lib.utils.common.api.DataMap;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSectorSnapshot;
import net.runeduniverse.tools.maven.r4m.pem.api.ModelPredicate;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelOverrideFilterSupplier;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public class DefaultSectorSnapshot implements ExecutionArchiveSectorSnapshot {

	protected final Map<ProjectExecutionModel, Set<Execution>> models = new LinkedHashMap<>();
	protected final Map<String, Set<Execution>> executions = new LinkedHashMap<>();
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
	public boolean hasModelWithEffectiveOverride(final boolean requireInherited) {
		for (ProjectExecutionModel pem : this.models.keySet()) {
			if (requireInherited && !pem.isInherited())
				continue;
			if (pem.isEffective())
				return true;
		}
		return false;
	}

	@Override
	public DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> getOverridesAsBooleanMapWithModels(
			boolean requireInherited) {
		return getOverridesAsBooleanMap(ModelOverride::type, requireInherited);
	}

	@Override
	public DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> getOverridesAsHintedBooleanMapWithModels(
			boolean requireInherited) {
		return getOverridesAsBooleanMap(ModelOverride::hint, requireInherited);
	}

	protected DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> getOverridesAsBooleanMap(
			final Function<ModelOverride, String> keySupplier, boolean requireInherited) {
		final DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> overrides = new LinkedHashDataMap<>(0);
		final Set<ProjectExecutionModel> models = new LinkedHashSet<>(this.models.keySet());
		// validate models & check all except the user-defined pems
		for (Iterator<ProjectExecutionModel> i = models.iterator(); i.hasNext();) {
			final ProjectExecutionModel pem = i.next();
			if (pem == null || requireInherited && pem.isInherited()) {
				i.remove();
				continue;
			}
			if (pem.isUserDefined())
				continue;
			for (Entry<String, ModelOverride> entry : pem.getOverridesAsMap()
					.entrySet()) {
				final ModelOverride override = entry.getValue();
				if (override == null)
					continue;
				final String key = keySupplier.apply(override);
				final AtomicBoolean active = overrides.computeIfAbsent(key, k -> new AtomicBoolean());
				if (active.get() || !override.isActive())
					continue;
				active.set(true);
				overrides.computeDataIfAbsent(key, k -> new LinkedHashSet<>(1))
						.add(pem);
			}
			i.remove();
		}
		// check the user-defined pems
		for (ProjectExecutionModel pem : models) {
			for (Entry<String, ModelOverride> entry : pem.getOverridesAsMap()
					.entrySet()) {
				final ModelOverride override = entry.getValue();
				if (override == null)
					continue;
				final String key = keySupplier.apply(override);
				final AtomicBoolean active = overrides.computeIfAbsent(key, k -> new AtomicBoolean());
				active.set(override.isActive());
				overrides.computeDataIfAbsent(key, k -> new LinkedHashSet<>(1))
						.add(pem);
			}
		}
		return overrides;
	}

	@Override
	public DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> collectOverridesAsBooleanMapWithModels() {
		return collectOverridesAsBooleanMap(ExecutionArchiveSectorSnapshot::getOverridesAsBooleanMapWithModels);
	}

	@Override
	public DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> collectOverridesAsHintedBooleanMapWithModels() {
		return collectOverridesAsBooleanMap(ExecutionArchiveSectorSnapshot::getOverridesAsHintedBooleanMapWithModels);
	}

	protected DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> collectOverridesAsBooleanMap(
			final BiFunction<ExecutionArchiveSectorSnapshot, Boolean, DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>>> fnc) {
		DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> domOverrides = fnc.apply(this, false);
		for (ExecutionArchiveSectorSnapshot parent = this.parent; parent != null; parent = parent.getParent()) {
			final DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> overrides = fnc.apply(parent, true);
			mergeOverrides(overrides, domOverrides);
			domOverrides = overrides;
		}
		return domOverrides;
	}

	protected void mergeOverrides(final DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> base,
			final DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> dominant) {
		dominant.forEach((key, domValue, domData) -> {
			final AtomicBoolean value = base.computeIfAbsent(key, k -> new AtomicBoolean());
			if (value.get() == domValue.get()) {
				// if the value does not change, add the dom models to the old ones
				base.computeDataIfAbsent(key, k -> new LinkedHashSet<>(0))
						.addAll(domData);
			} else {
				// if the value changes, replace the models with the dom models
				value.set(domValue.get());
				base.putData(key, domData);
			}
		});
	}

	@Override
	public void addModel(final ProjectExecutionModel pem) {
		if (pem == null)
			return;

		final Set<Execution> perModelSet = this.models.computeIfAbsent(pem, k -> new LinkedHashSet<>(1));
		for (Execution execution : pem.getExecutions()) {
			if (execution == null)
				continue;
			perModelSet.add(execution);
			this.executionOrigins.put(execution, pem);

			final Set<Execution> col = this.executions.computeIfAbsent(execution.getId(), k -> new HashSet<>());
			col.add(execution);
		}
	}

	@Override
	public ExecutionArchiveSectorSnapshot applyOverrides(final Map<String, AtomicBoolean> overrides,
			final Function<Map<String, AtomicBoolean>, ModelPredicate<ProjectExecutionModel, Execution>>... filterSupplier) {
		if (filterSupplier == null)
			return this;
		for (Function<Map<String, AtomicBoolean>, ModelPredicate<ProjectExecutionModel, Execution>> supplier : filterSupplier) {
			applyFilter(supplier.apply(overrides));
		}
		return this;
	}

	@Override
	public ExecutionArchiveSectorSnapshot applyOverrides(final Map<String, AtomicBoolean> overrides,
			final Collection<ProjectExecutionModelOverrideFilterSupplier> filterSupplier) {
		if (filterSupplier == null)
			return this;
		for (ProjectExecutionModelOverrideFilterSupplier supplier : filterSupplier) {
			applyFilter(supplier.get(overrides));
		}
		return this;
	}

	@Override
	public ExecutionArchiveSectorSnapshot applyFilter(final ModelPredicate<ProjectExecutionModel, Execution> filter) {
		for (Set<Execution> execCol : this.executions.values()) {
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

	@Override
	public Set<Execution> getExecutions() {
		return Collections.unmodifiableSet(this.executionOrigins.keySet());
	}

	@Override
	public Set<Execution> getExecutions(final String id) {
		return Collections.unmodifiableSet(this.executions.getOrDefault(id, Collections.emptySet()));
	}

	@Override
	public Set<Execution> getExecutions(final ModelPredicate<ProjectExecutionModel, Execution> filter) {
		final Set<Execution> executions = new LinkedHashSet<>();

		if (filter == null) {
			executions.addAll(this.executionOrigins.keySet());
			return executions;
		}

		for (Entry<Execution, ProjectExecutionModel> entry : this.executionOrigins.entrySet()) {
			// apply filter & collect data
			final Execution execution = entry.getKey();
			if (filter.test(entry.getValue(), execution))
				executions.add(execution);
		}
		return executions;
	}
}
