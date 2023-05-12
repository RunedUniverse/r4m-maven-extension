package net.runeduniverse.tools.maven.r4m.pem.writer.trigger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionTriggerWriter;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.trigger.ProfileTriggerInactive;

@Component(role = ExecutionTriggerWriter.class, hint = ProfileTriggerInactive.HINT)
public class ProfileTriggerInactiveWriter implements ExecutionTriggerWriter {

	@Override
	public boolean append(final PlexusConfiguration triggerNode, final ExecutionTrigger trigger) {
		if (trigger == null || !(trigger instanceof ProfileTriggerInactive))
			return false;

		PlexusConfiguration node = triggerNode.getChild(ProfileTriggerInactive.HINT, true);
		node.setAttribute("id", ((ProfileTriggerInactive) trigger).getProfileId());

		return true;
	}

}
