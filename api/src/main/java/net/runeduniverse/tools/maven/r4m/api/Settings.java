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
package net.runeduniverse.tools.maven.r4m.api;

import net.runeduniverse.lib.utils.config.api.Property;
import net.runeduniverse.lib.utils.config.api.PropertyStore;

public interface Settings extends PropertyStore {

	public static final String PROP_LIFECYCLE_TASK_REQUEST_CALCULATOR = "r4m.lifecycle-task-request-calculator";
	public static final String PROP_LIFECYCLE_TASK_REQUEST_CALCULATOR_ON_FORK = "r4m.lifecycle-task-request-calculator-on-fork";
	public static final String PROP_MISSING_BUILD_PLUGIN_HANDLER = "r4m.missing-build-plugin-handler";
	public static final String PROP_ACTIVE_PROFILES_INHERITANCE = "r4m.active-profiles-inheritance";
	public static final String PROP_SHOW_ACTIVE_OVERRIDES = "r4m.show-active-overrides";
	public static final String PROP_FANCY_OUTPUT = "r4m.fancy-output";
	public static final String PROP_PATCH_MOJO_ON_FORK = "r4m.patch-mojo-on-fork";
	public static final String PROP_GENERATE_PLUGIN_EXECUTIONS = "r4m.generate-plugin-executions";
	public static final String PROP_GENERATE_PLUGIN_EXECUTIONS_ON_FORK = "r4m.generate-plugin-executions-on-fork";
	public static final String PROP_MAVEN_BACKWARDS_COMPATIBLE = "r4m.maven-backwards-compatible";
	public static final String PROP_DEBUG_DUMP_GRM_ENTRIES_BEFORE_EXECUTION = "r4m.debug.dump-grm-entries-before-execution";

	public LoadState getLoadState();

	public Property<String> getLifecycleTaskRequestCalculator();

	public Property<String> getLifecycleTaskRequestCalculatorOnFork();

	public Property<String> getMissingBuildPluginHandler();

	public Property<String> getActiveProfilesInheritance();

	public Property<String> getShowActiveOverrides();

	public Property<Boolean> getFancyOutput();

	public Property<Boolean> getPatchMojoOnFork();

	public Property<Boolean> getGeneratePluginExecutions();

	public Property<Boolean> getGeneratePluginExecutionsOnFork();

	public Property<Boolean> getMavenBackwardsCompatible();

	public Property<String> getDebugDumpGrmEntriesBeforeExecution();

	public void setLoadState(LoadState state);

	public void setLifecycleTaskRequestCalculator(Property<String> value);

	public void setLifecycleTaskRequestCalculatorOnFork(Property<String> value);

	public void setMissingBuildPluginHandler(Property<String> value);

	public void setActiveProfilesInheritance(Property<String> value);

	public void setShowActiveOverrides(Property<String> value);

	public void setFancyOutput(Property<Boolean> value);

	public void setPatchMojoOnFork(Property<Boolean> value);

	public void setGeneratePluginExecutions(Property<Boolean> value);

	public void setGeneratePluginExecutionsOnFork(Property<Boolean> value);

	public void setMavenBackwardsCompatible(Property<Boolean> value);

	public void setDebugDumpGrmEntriesBeforeExecution(Property<String> value);

	public static enum LoadState {
		SYSTEM_EXTENSION, CORE_EXTENSION, BUILD_EXTENSION, PLUGIN
	}
}
