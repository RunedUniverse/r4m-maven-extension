package net.runeduniverse.tools.maven.r4m.pem.writer.restrictions;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionRestrictionWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.restrictions.PackagingProcedureRestriction;

@Component(role = ExecutionRestrictionWriter.class, hint = PackagingProcedureRestriction.HINT)
public class PackagingProcedureRestrictionWriter implements ExecutionRestrictionWriter {

	@Override
	public PlexusConfiguration convert(final ExecutionRestriction restriction) {
		if (restriction == null || !(restriction instanceof PackagingProcedureRestriction))
			return null;

		PlexusConfiguration node = new XmlPlexusConfiguration(PackagingProcedureRestriction.HINT);
		node.setValue(((PackagingProcedureRestriction) restriction).getPackagingProcedure());

		return node;
	}

}
