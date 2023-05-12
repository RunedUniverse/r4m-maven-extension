package net.runeduniverse.tools.maven.r4m.pem.restrictions;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;

public class PackagingProcedureRestriction implements ExecutionRestriction {

	public static final String HINT = "packaging-procedure";

	protected final String procedure;

	public PackagingProcedureRestriction(String packagingProcedure) {
		this.procedure = packagingProcedure;
	}

	public String getPackagingProcedure() {
		return this.procedure;
	}

	@Override
	public String getHint() {
		return PackagingProcedureRestriction.HINT;
	}

	@Override
	public boolean isActive(final ExecutionArchiveSelectorConfig config) {
		if (this.procedure == null)
			if (config.getPackagingProcedure() != null)
				return false;
		return this.procedure.equals(config.getPackagingProcedure());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PackagingProcedureRestriction))
			return false;
		PackagingProcedureRestriction restriction = (PackagingProcedureRestriction) obj;
		if (this.procedure == null)
			if (restriction.getPackagingProcedure() != null)
				return false;
		return this.procedure.equals(restriction.getPackagingProcedure());
	}

	@Override
	public CompoundTree toRecord() {
		return new CompoundTree("packaging-procedure", this.procedure);
	}
}
