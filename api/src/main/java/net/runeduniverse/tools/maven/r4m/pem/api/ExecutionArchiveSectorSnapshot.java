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
package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public interface ExecutionArchiveSectorSnapshot {

	public MavenProject getMvnProject();

	public ExecutionArchiveSectorSnapshot getParent();

	public ProjectExecutionModel getModel(Execution execution);

	public boolean hasModelWithEffectiveOverride();

	/**
	 * Get active overrides as boolean values indexed by model types.
	 *
	 * @return boolean map indexed by model types
	 */
	public Map<String, AtomicBoolean> getOverridesAsBooleanMap();

	/**
	 * Get active overrides as boolean values indexed by model hints.
	 *
	 * @return boolean map indexed by model hints
	 */
	public Map<String, AtomicBoolean> getOverridesAsBooleanMap2();

	/**
	 * Collects all active overrides as boolean values indexed by model types.
	 *
	 * @return boolean map indexed by model types
	 */
	public Map<String, AtomicBoolean> collectOverridesAsBooleanMap();

	/**
	 * Collects all active overrides as boolean values indexed by model hints.
	 *
	 * @return boolean map indexed by model hints
	 */
	public Map<String, AtomicBoolean> collectOverridesAsBooleanMap2();

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

	public void addModel(ProjectExecutionModel pem);

	@SuppressWarnings("unchecked")
	public ExecutionArchiveSectorSnapshot applyOverrides(Map<String, AtomicBoolean> overrides,
			Function<Map<String, AtomicBoolean>, ModelPredicate<ProjectExecutionModel, Execution>>... filterSupplier);

	public ExecutionArchiveSectorSnapshot applyOverrides(Map<String, AtomicBoolean> overrides,
			Collection<ProjectExecutionModelOverrideFilterSupplier> filterSupplier);

	public ExecutionArchiveSectorSnapshot applyFilter(ModelPredicate<ProjectExecutionModel, Execution> filter);

	public default ExecutionArchiveSectorSnapshot applyFilter(Predicate<Execution> filter) {
		return applyFilter(ModelPredicate.wrap(filter));
	}

}
