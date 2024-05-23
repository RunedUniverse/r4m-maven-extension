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
package net.runeduniverse.tools.maven.r4m.pem;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.pem.api.PluginExecutionRegistry;
import net.runeduniverse.tools.maven.r4m.pem.api.PluginExecutionRegistrySlice;

@Component(role = PluginExecutionRegistry.class, instantiationStrategy = "keep-alive")
public class Registry implements PluginExecutionRegistry {

	private Map<String, PluginExecutionRegistrySlice> prefixedSlices = new LinkedHashMap<>();
	private Map<String, PluginExecutionRegistrySlice> keyedSlices = new LinkedHashMap<>();
	private Map<String, PluginDescriptor> prefixedDescriptor = new LinkedHashMap<>();
	private Map<String, PluginDescriptor> keyedDescriptor = new LinkedHashMap<>();

	@Override
	public PluginExecutionRegistrySlice createSlice(PluginDescriptor mvnPluginDescriptor) {
		String prefix = mvnPluginDescriptor.getGoalPrefix();
		String key = createKey(mvnPluginDescriptor.getGroupId(), mvnPluginDescriptor.getArtifactId());

		PluginExecutionRegistrySlice slice = new RegistrySlice();
		if (!isBlank(prefix)) {
			this.prefixedDescriptor.put(prefix, mvnPluginDescriptor);
			this.prefixedSlices.put(prefix, slice);
		}

		this.keyedDescriptor.put(key, mvnPluginDescriptor);
		this.keyedSlices.put(key, slice);

		return slice;
	}

	@Override
	public boolean hasSlice(String prefix) {
		return this.prefixedSlices.containsKey(prefix);
	}

	@Override
	public boolean hasSlice(String groupId, String artifactId) {
		return this.prefixedSlices.containsKey(createKey(groupId, artifactId));
	}

	@Override
	public PluginExecutionRegistrySlice getSlice(String prefix) {
		return this.prefixedSlices.get(prefix);
	}

	@Override
	public PluginExecutionRegistrySlice getSlice(String groupId, String artifactId) {
		return this.keyedSlices.get(createKey(groupId, artifactId));
	}

	protected static String createKey(String groupId, String artifactId) {
		return String.join(":", groupId, artifactId);
	}

}
