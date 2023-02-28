package net.runeduniverse.tools.maven.r4m.pem.writer.trigger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionTriggerWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.trigger.ProfileTriggerProvided;

@Component(role = ExecutionTriggerWriter.class, hint = ProfileTriggerProvided.HINT)
public class ProfileTriggerProvidedWriter implements ExecutionTriggerWriter {

	@Override
	public PlexusConfiguration convert(final ExecutionTrigger trigger) {
		if (trigger == null || !(trigger instanceof ProfileTriggerProvided))
			return null;

		PlexusConfiguration node = new XmlPlexusConfiguration(ProfileTriggerProvided.HINT);
		node.setAttribute("id", ((ProfileTriggerProvided) trigger).getProfileId());

		return node;
	}

}
