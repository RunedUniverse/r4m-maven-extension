package net.runeduniverse.tools.maven.r4m.pem.parser.trigger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionTriggerParser;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Trigger;
import net.runeduniverse.tools.maven.r4m.pem.trigger.ProfileTriggerInactive;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ExecutionTriggerParser.class, hint = "inactive-profile")
public class ProfileTriggerInactiveParser implements ExecutionTriggerParser {

	@Override
	public Trigger parse(PlexusConfiguration node) {
		String id = node.getAttribute("id");
		if (isBlank(id))
			return null;
		return new ProfileTriggerInactive(id);
	}

}
