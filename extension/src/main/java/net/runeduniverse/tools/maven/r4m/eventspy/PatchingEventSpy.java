package net.runeduniverse.tools.maven.r4m.eventspy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.eventspy.api.MavenPluginPatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.MessagePatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.PatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.PatchingEvent.Type;

@Component(role = EventSpy.class, hint = PatchingEventSpy.HINT)
public class PatchingEventSpy implements EventSpy {

	public static final String HINT = Properties.PREFIX_ID + "-eventspy-patching";

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
		case INFO_PATCHING_STOPPED:
		case INFO_PATCHING_ABORTED:
			printBox(event);
			break;
		case WARN_UNIDENTIFIABLE_PLUGIN_DETECTED:
			warnUnidentifiablePlugins(event);
			break;
		case INFO_LIFECYCLE_EXEC_PLAN_CALC_STARTED:
		case INFO_LIFECYCLE_EXEC_PLAN_CALC_FINISHED:
			handlePatchingLifecycleExecutionPlan(event);
			break;
		case INFO_ELEVATING_TO_CORE_REALM:
		case INFO_ELEVATING_TO_BUILD_REALM:
			printExtensionState(event);
			break;

		default:
			break;
		}
	}

	@Override
	public void close() throws Exception {
	}

	private void printBox(PatchingEvent event) {
		if (event.getType() == Type.INFO_PATCHING_STARTED) {
			this.log.info("------------------------------------------------------------------------\033[m");
			this.log.info("\033[1mRunes4Maven Extension: patching Maven\033[m");
			this.log.info("");
		}
		if (event.getType() == Type.INFO_PATCHING_STOPPED)
			this.log.info("");
		if (event.getType() == Type.INFO_PATCHING_ABORTED)
			this.log.error("\033[1;31mpatching aborted\u001B[0m", event.getException());
	}

	private void printExtensionState(PatchingEvent event) {
		if (event.getType() == Type.INFO_ELEVATING_TO_CORE_REALM)
			this.log.info("State: Core-Extension");
		if (event.getType() == Type.INFO_ELEVATING_TO_BUILD_REALM) {
			this.log.info("State: Build-Extension");
			this.log.warn("Some features may not me available when loaded as Build-Extension!");
		}
	}

	private static final String WARN_UNIDENTIFIABLE_PLUGIN_DETECTED_HEAD = //
			"\033[1;31mFollowing Plugins or one of their dependencies could not be resolved:\u001B[0m";
	private static final String WARN_UNIDENTIFIABLE_PLUGIN_DETECTED = //
			"  » %s:%s:%s";

	private void warnUnidentifiablePlugins(PatchingEvent event) {
		Collection<Plugin> unidentifiablePlugins = new LinkedList<>();
		if (event instanceof MavenPluginPatchingEvent)
			unidentifiablePlugins.addAll(((MavenPluginPatchingEvent) event).getEffectedPlugins());
		if (unidentifiablePlugins.isEmpty())
			return;
		this.log.warn(WARN_UNIDENTIFIABLE_PLUGIN_DETECTED_HEAD);
		for (Plugin mvnPlugin : unidentifiablePlugins)
			this.log.warn(String.format(WARN_UNIDENTIFIABLE_PLUGIN_DETECTED, mvnPlugin.getGroupId(),
					mvnPlugin.getArtifactId(), mvnPlugin.getVersion()));
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

}
