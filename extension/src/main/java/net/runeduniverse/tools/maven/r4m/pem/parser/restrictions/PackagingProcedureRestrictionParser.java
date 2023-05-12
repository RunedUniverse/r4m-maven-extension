package net.runeduniverse.tools.maven.r4m.pem.parser.restrictions;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionRestrictionParser;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.restrictions.PackagingProcedureRestriction;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ExecutionRestrictionParser.class, hint = PackagingProcedureRestriction.HINT)
public class PackagingProcedureRestrictionParser implements ExecutionRestrictionParser {

	@Override
	public ExecutionRestriction parse(final PlexusConfiguration node) {
		String procedure = node.getValue();
		if (isBlank(procedure))
			return null;
		return new PackagingProcedureRestriction(procedure);
	}

}
