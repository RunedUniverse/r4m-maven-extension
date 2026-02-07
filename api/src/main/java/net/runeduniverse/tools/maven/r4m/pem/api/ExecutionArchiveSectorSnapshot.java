/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.common.api.DataMap;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public interface ExecutionArchiveSectorSnapshot {

	public MavenProject getMvnProject();

	public ExecutionArchiveSectorSnapshot getParent();

	public ProjectExecutionModel getModel(Execution execution);

	public boolean hasModelWithEffectiveOverride(boolean requireInherited);

	/**
	 * Get active overrides as boolean values indexed by model types.
	 *
	 * @param requireInherited ensures all models to be inherited
	 * @return boolean map indexed by model types
	 */
	public default Map<String, AtomicBoolean> getOverridesAsBooleanMap(boolean requireInherited) {
		return getOverridesAsBooleanMapWithData(requireInherited).toValueMap();
	}

	/**
	 * Get active overrides as boolean values indexed by model types.
	 *
	 * @param requireInherited ensures all models to be inherited
	 * @return boolean data-map indexed by model types with models
	 */
	public DataMap<String, AtomicBoolean, Data> getOverridesAsBooleanMapWithData(boolean requireInherited);

	/**
	 * Collects all active overrides as boolean values indexed by model types.
	 *
	 * @return boolean map indexed by model types
	 */
	public default Map<String, AtomicBoolean> collectOverridesAsBooleanMap() {
		return collectOverridesAsBooleanMapWithData().toValueMap();
	}

	/**
	 * Collects all active overrides as boolean values indexed by model hints.
	 *
	 * @return boolean data-map indexed by model hints with models
	 */
	public DataMap<String, AtomicBoolean, Data> collectOverridesAsBooleanMapWithData();

	/**
	 * Returns the internal reference from the internal types of overrides to their
	 * model based hints
	 *
	 * @return map mapping internal override types to model based hints
	 */
	public Map<String, String> getOverrideModelReference();

	public Set<Execution> getExecutions();

	public Set<Execution> getExecutions(String id);

	public Set<Execution> getExecutions(ModelPredicate<ProjectExecutionModel, Execution> filter);

	public default Set<Execution> getExecutions(ModelPredicate<ProjectExecutionModel, Execution> filter,
			boolean requireInherited) {
		return getExecutions(ModelPredicate.and( //
				requireInherited ? ExecutionFilterUtils::requireInheritedFilter : null, //
				filter //
		));
	}

	public default Set<Execution> getEffectiveExecutions(ModelPredicate<ProjectExecutionModel, Execution> filter,
			boolean requireInherited) {
		return getExecutions( //
				ModelPredicate.and(ExecutionFilterUtils::requireSuperPemFilter, filter), //
				requireInherited //
		);
	}

	public default Set<Execution> getUserDefinedExecutions(ModelPredicate<ProjectExecutionModel, Execution> filter,
			boolean requireInherited) {
		return getExecutions( //
				ModelPredicate.and(ExecutionFilterUtils::requireUserDefinedFilter, filter), //
				requireInherited //
		);
	}

	public void flushCache();

	public void addModel(ProjectExecutionModel pem);

	public void addDownstreamSnapshot(ExecutionArchiveSectorSnapshot snapshot);

	public ExecutionArchiveSectorSnapshot applyOverrides(
			DataMap<String, AtomicBoolean, ExecutionArchiveSectorSnapshot.Data> overrides,
			Collection<ProjectExecutionModelOverrideFilterSupplier> filterSupplier);

	public ExecutionArchiveSectorSnapshot applyFilter(ModelPredicate<ProjectExecutionModel, Execution> filter);

	public default ExecutionArchiveSectorSnapshot applyFilter(Predicate<Execution> filter) {
		return applyFilter(ModelPredicate.wrap(filter));
	}

	public static interface Data {

		public Set<ModelOverride> getModelOverrides();

		public Set<ProjectExecutionModel> getProjectExecutionModels();

		public Data copy();

		public Data merge(Data data);

	}

}
