package net.runeduniverse.tools.maven.r4m.pem.writer.trigger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionTriggerWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.trigger.ProfileTriggerActive;

@Component(role = ExecutionTriggerWriter.class, hint = ProfileTriggerActive.HINT)
public class ProfileTriggerActiveWriter implements ExecutionTriggerWriter {

	@Override
	public boolean append(final PlexusConfiguration triggerNode, final ExecutionTrigger trigger) {
		if (trigger == null || !(trigger instanceof ProfileTriggerActive))
			return false;

		PlexusConfiguration node = triggerNode.getChild(ProfileTriggerActive.HINT, true);
		node.setAttribute("id", ((ProfileTriggerActive) trigger).getProfileId());

		return true;
	}

}
