package net.runeduniverse.tools.maven.r4m.pem.writer.trigger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionTriggerWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.trigger.ProfileTriggerActive;

@Component(role = ExecutionTriggerWriter.class, hint = ProfileTriggerActive.HINT)
public class ProfileTriggerActiveWriter implements ExecutionTriggerWriter {

	@Override
	public PlexusConfiguration convert(final ExecutionTrigger trigger) {
		if (trigger == null || !(trigger instanceof ProfileTriggerActive))
			return null;

		PlexusConfiguration node = new XmlPlexusConfiguration(ProfileTriggerActive.HINT);
		node.setAttribute("id", ((ProfileTriggerActive) trigger).getProfileId());

		return node;
	}

}
