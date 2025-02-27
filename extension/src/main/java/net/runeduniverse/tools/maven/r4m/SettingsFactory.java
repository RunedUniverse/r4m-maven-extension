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

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.lib.utils.maven.ext.config.ConfigBuilder;
import net.runeduniverse.tools.maven.r4m.api.Settings;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskRequestCalculatorDelegate;

@Component(role = SettingsFactory.class)
public class SettingsFactory {

	@Requirement
	private Settings settings;
	@Requirement
	private PlexusContainer container;

	public void setup(final MavenSession mvnSession) {
		final ConfigBuilder<Settings> builder = new ConfigBuilder<>(settings);

		builder.withDefaultProperty(Settings.PROP_LIFECYCLE_TASK_REQUEST_CALCULATOR, "sequential");
		builder.withDefaultProperty(Settings.PROP_LIFECYCLE_TASK_REQUEST_CALCULATOR_ON_FORK, "sequential");
		builder.withDefaultProperty(Settings.PROP_MISSING_BUILD_PLUGIN_HANDLER, "warn");
		builder.withDefaultProperty(Settings.PROP_ACTIVE_PROFILES_INHERITANCE, "upstream");
		builder.withDefaultProperty(Settings.PROP_FANCY_OUTPUT, "true");
		builder.withDefaultProperty(Settings.PROP_PATCH_MOJO_ON_FORK, "true");
		builder.withDefaultProperty(Settings.PROP_GENERATE_PLUGIN_EXECUTIONS, "true");
		builder.withDefaultProperty(Settings.PROP_GENERATE_PLUGIN_EXECUTIONS_ON_FORK, "true");
		// debug
		builder.withDefaultProperty(Settings.PROP_DEBUG_DUMP_GRM_ENTRIES_BEFORE_EXECUTION, "reduced");

		builder.withProperties( //
				mvnSession::getSystemProperties, //
				mvnSession.getCurrentProject()::getProperties, //
				mvnSession::getUserProperties //
		);

		builder.parsePlexusHintProperty(Settings.PROP_LIFECYCLE_TASK_REQUEST_CALCULATOR, this.container,
				LifecycleTaskRequestCalculatorDelegate.class);
		builder.parsePlexusHintProperty(Settings.PROP_LIFECYCLE_TASK_REQUEST_CALCULATOR_ON_FORK, this.container,
				LifecycleTaskRequestCalculatorDelegate.class);
		builder.parseTextProperty(Settings.PROP_MISSING_BUILD_PLUGIN_HANDLER, "skip", "warn", "scan", "download");
		builder.parseTextProperty(Settings.PROP_ACTIVE_PROFILES_INHERITANCE, "upstream", "top-level", "false");
		builder.parseBooleanProperty(Settings.PROP_FANCY_OUTPUT);
		builder.parseBooleanProperty(Settings.PROP_PATCH_MOJO_ON_FORK);
		builder.parseBooleanProperty(Settings.PROP_GENERATE_PLUGIN_EXECUTIONS);
		builder.parseBooleanProperty(Settings.PROP_GENERATE_PLUGIN_EXECUTIONS_ON_FORK);

		// debug
		builder.parseTextProperty(Settings.PROP_DEBUG_DUMP_GRM_ENTRIES_BEFORE_EXECUTION, "all", "reduced");

		settings.resolveSelections();
	}
}
