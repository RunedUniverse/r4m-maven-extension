package net.runeduniverse.tools.maven.r4m.lifecycle.api;

import org.apache.maven.plugin.descriptor.PluginDescriptor;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;

public interface MojoExecutionData {

	public ExecutionArchiveSelectorConfig getExecutionArchiveSelectorConfig();

	public boolean isForking();

	public Fork getFork();

	public PluginDescriptor getLifecycleOverlayOrigin();

	public void setLifecycleOverlayOrigin(PluginDescriptor lifecycleOverlayOrigin);

}
