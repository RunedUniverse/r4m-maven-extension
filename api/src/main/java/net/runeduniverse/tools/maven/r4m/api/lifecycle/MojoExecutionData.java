package net.runeduniverse.tools.maven.r4m.api.lifecycle;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Fork;

public interface MojoExecutionData {

	public ExecutionArchiveSelectorConfig getExecutionArchiveSelectorConfig();

	public boolean isForking();

	public Fork getFork();

}
