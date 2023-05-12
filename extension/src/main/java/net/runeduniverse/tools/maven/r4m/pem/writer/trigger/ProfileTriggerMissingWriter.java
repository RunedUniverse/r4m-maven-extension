package net.runeduniverse.tools.maven.r4m.pem.writer.trigger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionTriggerWriter;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.trigger.ProfileTriggerMissing;

@Component(role = ExecutionTriggerWriter.class, hint = ProfileTriggerMissing.HINT)
public class ProfileTriggerMissingWriter implements ExecutionTriggerWriter {

	@Override
	public boolean append(final PlexusConfiguration triggerNode, final ExecutionTrigger trigger) {
		if (trigger == null || !(trigger instanceof ProfileTriggerMissing))
			return false;

		PlexusConfiguration node = triggerNode.getChild(ProfileTriggerMissing.HINT, true);
		node.setAttribute("id", ((ProfileTriggerMissing) trigger).getProfileId());

		return true;
	}

}
