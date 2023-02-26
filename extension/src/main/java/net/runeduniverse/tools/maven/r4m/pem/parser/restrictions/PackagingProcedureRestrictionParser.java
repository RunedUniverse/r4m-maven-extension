package net.runeduniverse.tools.maven.r4m.pem.parser.restrictions;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionRestrictionParser;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.restrictions.PackagingProcedureRestriction;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ExecutionRestrictionParser.class, hint = "packaging-procedure")
public class PackagingProcedureRestrictionParser implements ExecutionRestrictionParser {

	@Override
	public ExecutionRestriction parse(PlexusConfiguration node) {
		String procedure = node.getValue();
		if (isBlank(procedure))
			return null;
		return new PackagingProcedureRestriction(procedure);
	}

}
