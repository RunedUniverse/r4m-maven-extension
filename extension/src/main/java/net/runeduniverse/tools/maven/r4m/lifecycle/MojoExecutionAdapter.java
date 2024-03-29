/*
 * Copyright © 2024 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.tools.maven.r4m.lifecycle;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import net.runeduniverse.tools.maven.r4m.lifecycle.api.MojoExecutionData;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;

public class MojoExecutionAdapter extends MojoExecution implements MojoExecutionData {

	private final ExecutionArchiveSelectorConfig selectorConfig;

	private Fork fork = null;
	private PluginDescriptor lifecycleOverlayOrigin = null;

	public MojoExecutionAdapter(Plugin plugin, String goal, String executionId,
			ExecutionArchiveSelectorConfig selectorConfig) {
		super(plugin, goal, executionId);
		this.selectorConfig = selectorConfig;
	}

	public MojoExecutionAdapter(MojoDescriptor mojoDescriptor, ExecutionArchiveSelectorConfig selectorConfig) {
		super(mojoDescriptor);
		this.selectorConfig = selectorConfig;
	}

	public MojoExecutionAdapter(MojoDescriptor mojoDescriptor, String executionId, Source source,
			ExecutionArchiveSelectorConfig selectorConfig) {
		super(mojoDescriptor, executionId, source);
		this.selectorConfig = selectorConfig;
	}

	public MojoExecutionAdapter(MojoDescriptor mojoDescriptor, String executionId,
			ExecutionArchiveSelectorConfig selectorConfig) {
		super(mojoDescriptor, executionId);
		this.selectorConfig = selectorConfig;
	}

	public MojoExecutionAdapter(MojoDescriptor mojoDescriptor, Xpp3Dom configuration,
			ExecutionArchiveSelectorConfig selectorConfig) {
		super(mojoDescriptor, configuration);
		this.selectorConfig = selectorConfig;
	}

	@Override
	public ExecutionArchiveSelectorConfig getExecutionArchiveSelectorConfig() {
		return this.selectorConfig;
	}

	@Override
	public boolean isForking() {
		return this.fork != null && this.fork.isValid();
	}

	@Override
	public Fork getFork() {
		return this.fork;
	}

	@Override
	public PluginDescriptor getLifecycleOverlayOrigin() {
		return this.lifecycleOverlayOrigin;
	}

	public void setFork(Fork fork) {
		this.fork = fork;
	}

	@Override
	public void setLifecycleOverlayOrigin(PluginDescriptor overlayOrigin) {
		this.lifecycleOverlayOrigin = overlayOrigin;
	}

}
