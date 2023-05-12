package net.runeduniverse.tools.maven.r4m.pem.model;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.Recordable;

public interface ExecutionTrigger extends Recordable {

	public String getHint();

	public boolean isActive(final ExecutionArchiveSelectorConfig config);

}
