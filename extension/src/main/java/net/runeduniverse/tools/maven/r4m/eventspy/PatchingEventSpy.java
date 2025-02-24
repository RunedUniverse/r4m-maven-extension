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
package net.runeduniverse.tools.maven.r4m.eventspy;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.lib.utils.maven.ext.api.Extension;
import net.runeduniverse.tools.maven.r4m.R4MProperties;
import net.runeduniverse.tools.maven.r4m.eventspy.api.ExtensionPatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.MavenPluginPatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.MessagePatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.PatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.PatchingEvent.Type;

@Component(role = EventSpy.class, hint = PatchingEventSpy.HINT)
public class PatchingEventSpy implements EventSpy {

	public static final String HINT = R4MProperties.PREFIX_ID + "-eventspy-patching";

	@Requirement
	private Logger log;

	@Override
	public void init(Context context) throws Exception {
	}

	@Override
	public void onEvent(Object eventObj) throws Exception {

		if (!(eventObj instanceof PatchingEvent))
			return;

		PatchingEvent event = (PatchingEvent) eventObj;

		switch (event.getType()) {
		case INFO_PATCHING_STARTED:
		case INFO_PATCHING_FINISHED:
		case INFO_PATCHING_ABORTED:
			printBox(event);
			break;
		case INFO_ELEVATING_TO_CORE_REALM:
		case INFO_ELEVATING_TO_BUILD_REALM:
			printExtensionState(event);
			break;
		case INFO_EXTENSIONS_DETECTED:
			printDetectedExtensions(event);
			break;
		case WARN_UNIDENTIFIABLE_PLUGIN_DETECTED:
			warnUnidentifiablePlugins(event);
			break;
		case INFO_LIFECYCLE_EXEC_PLAN_CALC_STARTED:
		case INFO_LIFECYCLE_EXEC_PLAN_CALC_FINISHED:
			handlePatchingLifecycleExecutionPlan(event);
			break;
		case INFO_SCANNING_FOR_REFERENCED_PLUGINS_STARTED:
		case INFO_SCANNING_FOR_REFERENCED_PLUGINS_FINISHED:
		case INFO_SCANNING_FOR_REFERENCED_PLUGINS_BY_PROJECT_STARTED:
		case INFO_SCANNING_FOR_REFERENCED_PLUGINS_BY_PROJECT_FINISHED:
			handleScanningReferencedPlugins(event);
			break;
		default:
			break;
		}
	}

	@Override
	public void close() throws Exception {
	}

	protected String id(MavenProject mvnProject) {
		if (mvnProject == null)
			return null;
		return String.format("%s:%s:%s:%s", mvnProject.getGroupId(), mvnProject.getArtifactId(),
				mvnProject.getPackaging(), mvnProject.getVersion());
	}

	private void printBox(PatchingEvent event) {
		if (event.getType() == Type.INFO_PATCHING_STARTED) {
			this.log.info("------------------------------------------------------------------------\033[m");
			this.log.info("\033[1mRunes4Maven Extension: patching Maven\033[m");
			this.log.info("");
		}
		if (event.getType() == Type.INFO_PATCHING_FINISHED)
			this.log.info("");
		if (event.getType() == Type.INFO_PATCHING_ABORTED)
			this.log.error("\033[1;31mpatching aborted\u001B[0m", event.getException());
	}

	private void printExtensionState(PatchingEvent event) {
		if (event.getType() == Type.INFO_ELEVATING_TO_PLEXUS_REALM)
			this.log.info("State: Core-Extension (system)");
		if (event.getType() == Type.INFO_ELEVATING_TO_CORE_REALM)
			this.log.info("State: Core-Extension");
		if (event.getType() == Type.INFO_ELEVATING_TO_BUILD_REALM) {
			this.log.info("State: Build-Extension");
			this.log.warn("Some features may not me available when loaded as Build-Extension!");
		}
	}

	private static final String INFO_EXTENSIONS_DETECTED_HEAD = //
			"Maven Extensions:";
	private static final String INFO_EXTENSIONS_DETECTED_EXT = //
			"  » %s";
	private static final String INFO_EXTENSIONS_DETECTED_STATUS = //
			"    - %-51s [ %10s ]";

	private void printDetectedExtensions(PatchingEvent patchingEvent) {
		ExtensionPatchingEvent event = null;
		if (patchingEvent instanceof ExtensionPatchingEvent)
			event = (ExtensionPatchingEvent) patchingEvent;
		if (event == null) {
			return;
		}
		this.log.info(INFO_EXTENSIONS_DETECTED_HEAD);

		final Collection<MavenProject> projects = event.getProjects();
		final int size = projects.size();
		final Map<MavenProject, Boolean> status = new LinkedHashMap<>();
		for (Extension extension : event.getExtensions()) {
			int check = 0;
			this.log.info(String.format(INFO_EXTENSIONS_DETECTED_EXT, extension.getId()));
			for (MavenProject project : projects) {
				if (extension.isPlugin(project)) {
					check = check + 1;
					status.put(project, true);
				} else {
					check = check + 2;
					status.put(project, false);
				}
			}
			if (size != 0) {
				if (check % size == 0) {
					if (check / size == 1) {
						// print ext+plugin
						this.log.info(String.format(INFO_EXTENSIONS_DETECTED_STATUS, "< all >", "ext+plugin"));
						continue;
					}
					// else: act as if size == 0
				} else {
					// print extension || ext+plugin
					for (Entry<MavenProject, Boolean> entry : status.entrySet()) {
						this.log.info(String.format(INFO_EXTENSIONS_DETECTED_STATUS, id(entry.getKey()),
								entry.getValue() ? "ext+plugin" : "extension"));
					}
					continue;
				}
			}
			// print extension
			this.log.info(String.format(INFO_EXTENSIONS_DETECTED_STATUS, "<all>", "extension"));
		}
		this.log.info("");
	}

	private static final String WARN_UNIDENTIFIABLE_PLUGIN_DETECTED_HEAD = //
			"\033[1;31mFollowing Plugins or one of their dependencies could not be resolved:\u001B[0m";
	private static final String WARN_UNIDENTIFIABLE_PLUGIN_DETECTED_PROJECT = //
			"  project: %s";
	private static final String WARN_UNIDENTIFIABLE_PLUGIN_DETECTED = //
			"  » %s:%s:%s";

	private void warnUnidentifiablePlugins(PatchingEvent event) {
		Map<MavenProject, Set<Plugin>> unidentifiablePluginsMap = new LinkedHashMap<>();
		if (event instanceof MavenPluginPatchingEvent)
			unidentifiablePluginsMap.putAll(((MavenPluginPatchingEvent) event).getEffectedPluginsPerProject());
		if (unidentifiablePluginsMap.isEmpty())
			return;
		this.log.warn(WARN_UNIDENTIFIABLE_PLUGIN_DETECTED_HEAD);
		for (Entry<MavenProject, Set<Plugin>> entry : unidentifiablePluginsMap.entrySet()) {
			final MavenProject mvnProject = entry.getKey();
			this.log.warn(String.format(WARN_UNIDENTIFIABLE_PLUGIN_DETECTED_PROJECT, id(mvnProject)));
			for (Plugin mvnPlugin : entry.getValue())
				this.log.warn(String.format(WARN_UNIDENTIFIABLE_PLUGIN_DETECTED, mvnPlugin.getGroupId(),
						mvnPlugin.getArtifactId(), mvnPlugin.getVersion()));
		}
		this.log.info("");
	}

	private static final String WARN_FAILED_TO_LOCATE_PLEXUS_COMPONENT = //
			"Component<%s> could not be located in PlexusContainer!";
	private static final String WARN_FAILED_TO_RELEASE_PLEXUS_COMPONENT = //
			"Component<%s> could not be released from PlexusContainer!";
	private static final String DEBUG_UPDATING_PLEXUS_COMPONENT_DESCRIPTOR = //
			"Updating ComponentDescriptor of Component<%s> to Role<%s>\tHint: %s";

	private void handlePatchingLifecycleExecutionPlan(PatchingEvent event) {
		Map<String, CharSequence> data = null;
		if (event instanceof MessagePatchingEvent)
			data = ((MessagePatchingEvent) event).getMessage();
		if (data == null) {
			if (event.getType() == Type.INFO_LIFECYCLE_EXEC_PLAN_CALC_STARTED)
				this.log.debug("patching LifecycleExecutionPlanCalculator");
			return;
		}

		switch (event.getType()) {
		case WARN_LIFECYCLE_EXEC_PLAN_CALC_FAILED_TO_LOCATE_PLEXUS_COMPONENT:
			this.log.warn(String.format(WARN_FAILED_TO_LOCATE_PLEXUS_COMPONENT, data.get("component")));
			break;
		case WARN_LIFECYCLE_EXEC_PLAN_CALC_FAILED_TO_RELEASE_PLEXUS_COMPONENT:
			this.log.warn(String.format(WARN_FAILED_TO_RELEASE_PLEXUS_COMPONENT, data.get("component")));
			break;
		case DEBUG_LIFECYCLE_EXEC_PLAN_CALC_UPDATING_PLEXUS_COMPONENT_DESCRIPTOR:
			this.log.debug(String.format(DEBUG_UPDATING_PLEXUS_COMPONENT_DESCRIPTOR, data.get("component"),
					data.get("role"), data.get("hint")));
			break;
		default:
			break;
		}
	}

	private static final String INFO_SCANNING_BUNDLED_PLUGINS_STARTED = //
			"searching for embedded plugins in projects:";
	private static final String INFO_SCANNING_BUNDLED_PLUGINS_RESULT = //
			"  %-54s [%3s | %2.3f s]";
	private Map<CharSequence, StopWatch> scanRefPluginsStopWatches = new LinkedHashMap<>();

	private void handleScanningReferencedPlugins(PatchingEvent event) {
		Map<String, CharSequence> data = null;
		if (event instanceof MessagePatchingEvent)
			data = ((MessagePatchingEvent) event).getMessage();
		if (data != null) {
			if (event.getType() == Type.INFO_SCANNING_FOR_REFERENCED_PLUGINS_BY_PROJECT_STARTED) {
				StopWatch watch = this.scanRefPluginsStopWatches.get(data.get("maven-project"));
				if (watch == null) {
					watch = new StopWatch();
					this.scanRefPluginsStopWatches.put(data.get("maven-project"), watch);
				} else
					watch.reset();
				watch.start();
				return;
			}
			if (event.getType() == Type.INFO_SCANNING_FOR_REFERENCED_PLUGINS_BY_PROJECT_FINISHED) {
				StopWatch watch = this.scanRefPluginsStopWatches.get(data.get("maven-project"));
				if (watch == null || !watch.isStarted())
					return;
				watch.stop();
				long elapsedTime = watch.getTime(TimeUnit.MILLISECONDS);
				this.log.info(String.format(INFO_SCANNING_BUNDLED_PLUGINS_RESULT, data.get("maven-project"),
						data.get("amount"), (double) elapsedTime / 1000));
				return;
			}
		}
		if (event.getType() == Type.INFO_SCANNING_FOR_REFERENCED_PLUGINS_STARTED) {
			this.log.info("");
			this.log.info(INFO_SCANNING_BUNDLED_PLUGINS_STARTED);
		}
	}

}
