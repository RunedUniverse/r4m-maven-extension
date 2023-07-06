package net.runeduniverse.tools.maven.r4m.pem.parser.trigger;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionTriggerParser;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.trigger.ProfileTriggerInactive;

@Component(role = ExecutionTriggerParser.class, hint = ProfileTriggerInactive.HINT)
public class ProfileTriggerInactiveParser implements ExecutionTriggerParser {

	@Override
	public ExecutionTrigger<ExecutionArchiveSelectorConfig> parse(final PlexusConfiguration node) {
		String id = node.getAttribute("id");
		if (isBlank(id))
			return null;
		return new ProfileTriggerInactive(id);
	}

}
