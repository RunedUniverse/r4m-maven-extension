package net.runeduniverse.tools.maven.r4m.api.pem.model;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.api.pem.Recordable;

public interface Trigger extends Recordable {
	public boolean isActive(ExecutionArchiveSelectorConfig config);
}
