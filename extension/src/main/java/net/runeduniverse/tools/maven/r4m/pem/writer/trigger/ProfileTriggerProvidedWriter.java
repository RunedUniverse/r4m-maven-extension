package net.runeduniverse.tools.maven.r4m.pem.writer.trigger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionTriggerWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.trigger.ProfileTriggerProvided;

@Component(role = ExecutionTriggerWriter.class, hint = ProfileTriggerProvided.HINT)
public class ProfileTriggerProvidedWriter implements ExecutionTriggerWriter {

	@Override
	public boolean append(final PlexusConfiguration triggerNode, final ExecutionTrigger trigger) {
		if (trigger == null || !(trigger instanceof ProfileTriggerProvided))
			return false;

		PlexusConfiguration node = triggerNode.getChild(ProfileTriggerProvided.HINT, true);
		node.setAttribute("id", ((ProfileTriggerProvided) trigger).getProfileId());

		return true;
	}

}
