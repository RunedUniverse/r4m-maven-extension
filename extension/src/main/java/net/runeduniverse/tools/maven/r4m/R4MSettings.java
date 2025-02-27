/*
 * Copyright © 2024 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m;

import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.lib.utils.maven.ext.config.APropertyStore;
import net.runeduniverse.lib.utils.maven.ext.config.api.Property;
import net.runeduniverse.tools.maven.r4m.api.Settings;

@Component(role = Settings.class, instantiationStrategy = "keep-alive")
public class R4MSettings extends APropertyStore implements Settings {

	private Property<String> lifecycleTaskRequestCalculator = null;
	private Property<String> lifecycleTaskRequestCalculatorOnFork = null;
	private Property<String> missingBuildPluginHandler = null;
	private Property<String> activeProfilesInheritance = null;
	private Property<Boolean> fancyOutput = null;
	private Property<Boolean> patchMojoOnFork = null;
	private Property<Boolean> generatePluginExecutions = null;
	private Property<Boolean> generatePluginExecutionsOnFork = null;
	private Property<String> debugDumpGrmEntriesBeforeExecution = null;

	public R4MSettings() {
		super(new ConcurrentHashMap<>());
	}

	//////////////////////////////////////////////////////////////////////////

	@Override
	public Property<String> getLifecycleTaskRequestCalculator() {
		return this.lifecycleTaskRequestCalculator;
	}

	@Override
	public Property<String> getLifecycleTaskRequestCalculatorOnFork() {
		return this.lifecycleTaskRequestCalculatorOnFork;
	}

	@Override
	public Property<String> getMissingBuildPluginHandler() {
		return this.missingBuildPluginHandler;
	}

	@Override
	public Property<String> getActiveProfilesInheritance() {
		return this.activeProfilesInheritance;
	}

	@Override
	public Property<Boolean> getFancyOutput() {
		return this.fancyOutput;
	}

	@Override
	public Property<Boolean> getPatchMojoOnFork() {
		return this.patchMojoOnFork;
	}

	@Override
	public Property<Boolean> getGeneratePluginExecutions() {
		return this.generatePluginExecutions;
	}

	@Override
	public Property<Boolean> getGeneratePluginExecutionsOnFork() {
		return this.generatePluginExecutionsOnFork;
	}

	@Override
	public Property<String> getDebugDumpGrmEntriesBeforeExecution() {
		return this.debugDumpGrmEntriesBeforeExecution;
	}

	//////////////////////////////////////////////////////////////////////////

	@Override
	public void setLifecycleTaskRequestCalculator(Property<String> value) {
		this.lifecycleTaskRequestCalculator = value;
		super.redirectSetProperty(PROP_LIFECYCLE_TASK_REQUEST_CALCULATOR, value);
	}

	@Override
	public void setLifecycleTaskRequestCalculatorOnFork(Property<String> value) {
		this.lifecycleTaskRequestCalculatorOnFork = value;
		super.redirectSetProperty(PROP_LIFECYCLE_TASK_REQUEST_CALCULATOR_ON_FORK, value);
	}

	@Override
	public void setMissingBuildPluginHandler(Property<String> value) {
		this.missingBuildPluginHandler = value;
		super.redirectSetProperty(PROP_MISSING_BUILD_PLUGIN_HANDLER, value);
	}

	@Override
	public void setActiveProfilesInheritance(Property<String> value) {
		this.activeProfilesInheritance = value;
		super.redirectSetProperty(PROP_ACTIVE_PROFILES_INHERITANCE, value);
	}

	@Override
	public void setFancyOutput(Property<Boolean> value) {
		this.fancyOutput = value;
		super.redirectSetProperty(PROP_FANCY_OUTPUT, value);
	}

	@Override
	public void setPatchMojoOnFork(Property<Boolean> value) {
		this.patchMojoOnFork = value;
		super.redirectSetProperty(PROP_PATCH_MOJO_ON_FORK, value);
	}

	@Override
	public void setGeneratePluginExecutions(Property<Boolean> value) {
		this.generatePluginExecutions = value;
		super.redirectSetProperty(PROP_GENERATE_PLUGIN_EXECUTIONS, value);
	}

	@Override
	public void setGeneratePluginExecutionsOnFork(Property<Boolean> value) {
		this.generatePluginExecutionsOnFork = value;
		super.redirectSetProperty(PROP_GENERATE_PLUGIN_EXECUTIONS_ON_FORK, value);
	}

	@Override
	public void setDebugDumpGrmEntriesBeforeExecution(Property<String> value) {
		this.debugDumpGrmEntriesBeforeExecution = value;
		super.redirectSetProperty(PROP_DEBUG_DUMP_GRM_ENTRIES_BEFORE_EXECUTION, value);
	}

	//////////////////////////////////////////////////////////////////////////

	@Override
	protected void redirectSetProperty(final String id, final Property<?> value) {
		switch (value.getId()) {
		case PROP_ACTIVE_PROFILES_INHERITANCE:
			tryCast(String.class, value, this::setActiveProfilesInheritance);
			break;
		case PROP_FANCY_OUTPUT:
			tryCast(Boolean.class, value, this::setFancyOutput);
			break;
		case PROP_GENERATE_PLUGIN_EXECUTIONS:
			tryCast(Boolean.class, value, this::setGeneratePluginExecutions);
			break;
		case PROP_GENERATE_PLUGIN_EXECUTIONS_ON_FORK:
			tryCast(Boolean.class, value, this::setGeneratePluginExecutionsOnFork);
			break;
		case PROP_LIFECYCLE_TASK_REQUEST_CALCULATOR:
			tryCast(String.class, value, this::setLifecycleTaskRequestCalculator);
			break;
		case PROP_LIFECYCLE_TASK_REQUEST_CALCULATOR_ON_FORK:
			tryCast(String.class, value, this::setLifecycleTaskRequestCalculatorOnFork);
			break;
		case PROP_MISSING_BUILD_PLUGIN_HANDLER:
			tryCast(String.class, value, this::setMissingBuildPluginHandler);
			break;
		case PROP_PATCH_MOJO_ON_FORK:
			tryCast(Boolean.class, value, this::setPatchMojoOnFork);
			break;
		case PROP_DEBUG_DUMP_GRM_ENTRIES_BEFORE_EXECUTION:
			tryCast(String.class, value, this::setDebugDumpGrmEntriesBeforeExecution);
			break;
		}
	}
}
