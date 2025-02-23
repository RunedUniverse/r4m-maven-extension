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

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.api.Property;
import net.runeduniverse.tools.maven.r4m.api.Settings;

@Component(role = Settings.class, instantiationStrategy = "keep-alive")
public class R4MSettings implements Settings {

	private final Map<String, Property<?>> properties = new LinkedHashMap<>();

	private Property<String> lifecycleTaskRequestCalculator = null;
	private Property<String> lifecycleTaskRequestCalculatorOnFork = null;
	private Property<String> missingBuildPluginHandler = null;
	private Property<String> activeProfilesInheritance = null;
	private Property<Boolean> fancyOutput = null;
	private Property<Boolean> patchMojoOnFork = null;
	private Property<Boolean> generatePluginExecutions = null;
	private Property<Boolean> generatePluginExecutionsOnFork = null;
	private Property<String> debugDumpGrmEntriesBeforeExecution = null;

	@Override
	public Collection<Property<?>> getAllProperties() {
		synchronized (this.properties) {
			return Collections.unmodifiableCollection(this.properties.values());
		}
	}

	@Override
	public void selectDefaults() {
		synchronized (this.properties) {
			for (Property<?> property : this.properties.values())
				selectDefault(property);
		}
	}

	public <T> void selectDefault(Property<T> property) {
		if (property == null)
			return;
		if (property.getSelected() == null)
			property.setSelected(property.getDefault());
	}

	@Override
	public Property<?> getProperty(String id) {
		synchronized (this.properties) {
			return this.properties.get(id);
		}
	}

	@Override
	public void setProperty(Property<?> value) {
		if (value == null || isBlank(value.getId()))
			return;
		if (!redirectSetProperty(value))
			synchronized (this.properties) {
				this.properties.put(value.getId(), value);
			}
	}

	@Override
	public void removeProperty(String id) {
		if (isBlank(id))
			return;
		if (!redirectRemoveProperty(id))
			synchronized (this.properties) {
				this.properties.remove(id);
			}
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
		synchronized (this.properties) {
			this.properties.values()
					.remove(this.lifecycleTaskRequestCalculator);
			if (value != null)
				this.properties.put(value.getId(), value);
			this.lifecycleTaskRequestCalculator = value;
		}
	}

	@Override
	public void setLifecycleTaskRequestCalculatorOnFork(Property<String> value) {
		synchronized (this.properties) {
			this.properties.values()
					.remove(this.lifecycleTaskRequestCalculatorOnFork);
			if (value != null)
				this.properties.put(value.getId(), value);
			this.lifecycleTaskRequestCalculatorOnFork = value;
		}

	}

	@Override
	public void setMissingBuildPluginHandler(Property<String> value) {
		synchronized (this.properties) {
			this.properties.values()
					.remove(this.missingBuildPluginHandler);
			if (value != null)
				this.properties.put(value.getId(), value);
			this.missingBuildPluginHandler = value;
		}
	}

	@Override
	public void setActiveProfilesInheritance(Property<String> value) {
		synchronized (this.properties) {
			this.properties.values()
					.remove(this.activeProfilesInheritance);
			if (value != null)
				this.properties.put(value.getId(), value);
			this.activeProfilesInheritance = value;
		}
	}

	@Override
	public void setFancyOutput(Property<Boolean> value) {
		synchronized (this.properties) {
			this.properties.values()
					.remove(this.fancyOutput);
			if (value != null)
				this.properties.put(value.getId(), value);
			this.fancyOutput = value;
		}
	}

	@Override
	public void setPatchMojoOnFork(Property<Boolean> value) {
		synchronized (this.properties) {
			this.properties.values()
					.remove(this.patchMojoOnFork);
			if (value != null)
				this.properties.put(value.getId(), value);
			this.patchMojoOnFork = value;
		}
	}

	@Override
	public void setGeneratePluginExecutions(Property<Boolean> value) {
		synchronized (this.properties) {
			this.properties.values()
					.remove(this.generatePluginExecutions);
			if (value != null)
				this.properties.put(value.getId(), value);
			this.generatePluginExecutions = value;
		}
	}

	@Override
	public void setGeneratePluginExecutionsOnFork(Property<Boolean> value) {
		synchronized (this.properties) {
			this.properties.values()
					.remove(this.generatePluginExecutionsOnFork);
			if (value != null)
				this.properties.put(value.getId(), value);
			this.generatePluginExecutionsOnFork = value;
		}
	}

	@Override
	public void setDebugDumpGrmEntriesBeforeExecution(Property<String> value) {
		synchronized (this.properties) {
			this.properties.values()
					.remove(this.debugDumpGrmEntriesBeforeExecution);
			if (value != null)
				this.properties.put(value.getId(), value);
			this.debugDumpGrmEntriesBeforeExecution = value;
		}
	}

	//////////////////////////////////////////////////////////////////////////

	protected boolean redirectSetProperty(Property<?> value) {
		switch (value.getId()) {
		case "r4m.active-profiles-inheritance":
			setActiveProfilesInheritance((Property<String>) value);
			return true;
		case "r4m.fancy-output":
			setFancyOutput((Property<Boolean>) value);
			return true;
		case "r4m.generate-plugin-executions":
			setGeneratePluginExecutions((Property<Boolean>) value);
			return true;
		case "r4m.generate-plugin-executions-on-fork":
			setGeneratePluginExecutionsOnFork((Property<Boolean>) value);
			return true;
		case "r4m.lifecycle-task-request-calculator":
			setLifecycleTaskRequestCalculator((Property<String>) value);
			return true;
		case "r4m.lifecycle-task-request-calculator-on-fork":
			setLifecycleTaskRequestCalculatorOnFork((Property<String>) value);
			return true;
		case "r4m.missing-build-plugin-handler":
			setMissingBuildPluginHandler((Property<String>) value);
			return true;
		case "r4m.patch-mojo-on-fork":
			setPatchMojoOnFork((Property<Boolean>) value);
			return true;
		case "r4m.debug.dump-grm-entries-before-execution":
			setDebugDumpGrmEntriesBeforeExecution((Property<String>) value);
			return true;
		}
		return false;
	}

	protected boolean redirectRemoveProperty(String id) {
		switch (id) {
		case "r4m.active-profiles-inheritance":
			setActiveProfilesInheritance(null);
			return true;
		case "r4m.fancy-output":
			setFancyOutput(null);
			return true;
		case "r4m.generate-plugin-executions":
			setGeneratePluginExecutions(null);
			return true;
		case "r4m.generate-plugin-executions-on-fork":
			setGeneratePluginExecutionsOnFork(null);
			return true;
		case "r4m.lifecycle-task-request-calculator":
			setLifecycleTaskRequestCalculator(null);
			return true;
		case "r4m.lifecycle-task-request-calculator-on-fork":
			setLifecycleTaskRequestCalculatorOnFork(null);
			return true;
		case "r4m.missing-build-plugin-handler":
			setMissingBuildPluginHandler(null);
			return true;
		case "r4m.patch-mojo-on-fork":
			setPatchMojoOnFork(null);
			return true;
		case "debug.dump-grm-entries-before-execution":
			setDebugDumpGrmEntriesBeforeExecution(null);
			return true;
		}
		return false;
	}
}
