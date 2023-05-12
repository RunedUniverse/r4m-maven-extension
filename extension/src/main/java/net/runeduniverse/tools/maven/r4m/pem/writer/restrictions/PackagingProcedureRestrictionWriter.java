package net.runeduniverse.tools.maven.r4m.pem.writer.restrictions;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionRestrictionWriter;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.restrictions.PackagingProcedureRestriction;

@Component(role = ExecutionRestrictionWriter.class, hint = PackagingProcedureRestriction.HINT)
public class PackagingProcedureRestrictionWriter implements ExecutionRestrictionWriter {

	@Override
	public boolean append(final PlexusConfiguration restrictionsNode, final ExecutionRestriction<?> restriction) {
		if (restriction == null || !(restriction instanceof PackagingProcedureRestriction))
			return false;

		restrictionsNode.addChild(PackagingProcedureRestriction.HINT,
				((PackagingProcedureRestriction) restriction).getPackagingProcedure());

		return true;
	}

}
