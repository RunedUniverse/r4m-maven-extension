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
package net.runeduniverse.tools.maven.r4m.indexer;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import net.runeduniverse.tools.maven.r4m.indexer.api.PluginBoundRegistry;

public abstract class APluginBoundRegistry<S> implements PluginBoundRegistry<S> {

	protected final Map<String, S> prefixedSectors = new LinkedHashMap<>();
	protected final Map<String, S> keyedSectors = new LinkedHashMap<>();
	protected final Map<String, PluginDescriptor> prefixedDescriptor = new LinkedHashMap<>();
	protected final Map<String, PluginDescriptor> keyedDescriptor = new LinkedHashMap<>();

	protected abstract S newSector(PluginDescriptor mvnPluginDescriptor);

	@Override
	public S createSector(PluginDescriptor mvnPluginDescriptor) {
		final String prefix = mvnPluginDescriptor.getGoalPrefix();
		final String key = createKey(mvnPluginDescriptor.getGroupId(), mvnPluginDescriptor.getArtifactId());

		final S sector = newSector(mvnPluginDescriptor);
		if (!isBlank(prefix)) {
			this.prefixedDescriptor.put(prefix, mvnPluginDescriptor);
			this.prefixedSectors.put(prefix, sector);
		}

		this.keyedDescriptor.put(key, mvnPluginDescriptor);
		this.keyedSectors.put(key, sector);

		return sector;
	}

	@Override
	public boolean hasSector(String prefix) {
		return this.prefixedSectors.containsKey(prefix);
	}

	@Override
	public boolean hasSector(String groupId, String artifactId) {
		return this.prefixedSectors.containsKey(createKey(groupId, artifactId));
	}

	@Override
	public S getSector(String prefix) {
		return this.prefixedSectors.get(prefix);
	}

	@Override
	public S getSector(String groupId, String artifactId) {
		return this.keyedSectors.get(createKey(groupId, artifactId));
	}

	protected static String createKey(String groupId, String artifactId) {
		return String.join(":", groupId, artifactId);
	}
}
