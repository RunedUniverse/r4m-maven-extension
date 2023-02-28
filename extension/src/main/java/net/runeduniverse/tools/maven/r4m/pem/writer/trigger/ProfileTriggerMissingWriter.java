package net.runeduniverse.tools.maven.r4m.pem.writer.trigger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionTriggerWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.trigger.ProfileTriggerMissing;

@Component(role = ExecutionTriggerWriter.class, hint = ProfileTriggerMissing.HINT)
public class ProfileTriggerMissingWriter implements ExecutionTriggerWriter {

	@Override
	public PlexusConfiguration convert(final ExecutionTrigger trigger) {
		if (trigger == null || !(trigger instanceof ProfileTriggerMissing))
			return null;

		PlexusConfiguration node = new XmlPlexusConfiguration(ProfileTriggerMissing.HINT);
		node.setAttribute("id", ((ProfileTriggerMissing) trigger).getProfileId());

		return node;
	}

}
