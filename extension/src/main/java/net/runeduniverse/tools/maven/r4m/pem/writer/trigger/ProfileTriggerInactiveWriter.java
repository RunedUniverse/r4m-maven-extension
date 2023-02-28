package net.runeduniverse.tools.maven.r4m.pem.writer.trigger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionTriggerWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.trigger.ProfileTriggerInactive;

@Component(role = ExecutionTriggerWriter.class, hint = ProfileTriggerInactive.HINT)
public class ProfileTriggerInactiveWriter implements ExecutionTriggerWriter {

	@Override
	public PlexusConfiguration convert(final ExecutionTrigger trigger) {
		if (trigger == null || !(trigger instanceof ProfileTriggerInactive))
			return null;

		PlexusConfiguration node = new XmlPlexusConfiguration(ProfileTriggerInactive.HINT);
		node.setAttribute("id", ((ProfileTriggerInactive) trigger).getProfileId());

		return node;
	}

}
