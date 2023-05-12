package net.runeduniverse.tools.maven.r4m.pem.parser.trigger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionTriggerParser;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.trigger.ProfileTriggerProvided;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ExecutionTriggerParser.class, hint = ProfileTriggerProvided.HINT)
public class ProfileTriggerProvidedParser implements ExecutionTriggerParser {

	@Override
	public ExecutionTrigger<ExecutionArchiveSelectorConfig> parse(final PlexusConfiguration node) {
		String id = node.getAttribute("id");
		if (isBlank(id))
			return null;
		return new ProfileTriggerProvided(id);
	}

}
