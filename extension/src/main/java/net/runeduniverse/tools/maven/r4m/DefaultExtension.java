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
package net.runeduniverse.tools.maven.r4m;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.StringUtils;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.aether.RepositorySystemSession;

import net.runeduniverse.tools.maven.r4m.api.Extension;

public class DefaultExtension implements Extension {

	protected ClassRealm realm;
	protected String groupId = null;
	protected String artifactId = null;
	protected String version = null;

	protected Plugin basePlugin = null;
	protected Map<MavenProject, Plugin> plugins = new LinkedHashMap<>(1);
	protected Map<MavenProject, PluginDescriptor> descriptors = new LinkedHashMap<>(1);

	@Override
	public ClassRealm getClassRealm() {
		return this.realm;
	}

	@Override
	public String getGroupId() {
		return this.groupId;
	}

	@Override
	public String getArtifactId() {
		return this.artifactId;
	}

	@Override
	public String getVersion() {
		return this.version;
	}

	@Override
	public String getId() {
		final StringBuilder builder = new StringBuilder(String.format("%s:%s", getGroupId(), getArtifactId()));
		final String version = getVersion();
		if (!StringUtils.isBlank(version))
			builder.append(String.format(":%s", version));
		return builder.toString();
	}

	@Override
	public boolean isPlugin(final MavenProject mvnProject) {
		return this.descriptors.get(mvnProject) != null;
	}

	@Override
	public PluginDescriptor getPluginDescriptor(final MavenProject mvnProject) {
		return this.descriptors.get(mvnProject);
	}

	@Override
	public void setClassRealm(ClassRealm realm) {
		this.realm = realm;
	}

	@Override
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	@Override
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public void setPlugin(Plugin plugin) {
		this.basePlugin = plugin;
	}

	@Override
	public void setPlugin(final MavenProject mvnProject, Plugin plugin) {
		this.plugins.put(mvnProject, plugin);
	}

	@Override
	public void setPluginDescriptor(final MavenProject mvnProject, PluginDescriptor descriptor) {
		this.descriptors.put(mvnProject, descriptor);
		if (descriptor != null) {
			if (descriptor.getPlugin() == null) {
				descriptor.setPlugin(asPlugin(mvnProject));
			} else {
				setPlugin(mvnProject, descriptor.getPlugin());
			}
		}
	}

	protected Plugin createPlugin() {
		final Plugin plugin = new Plugin();
		plugin.setExtensions(true);
		plugin.setGroupId(this.groupId);
		plugin.setArtifactId(this.artifactId);
		plugin.setVersion(this.version);
		return plugin;
	}

	@Override
	public Plugin asPlugin() {
		if (this.basePlugin == null) {
			this.basePlugin = createPlugin();
		}
		return this.basePlugin;
	}

	@Override
	public Plugin asPlugin(final MavenProject mvnProject) {
		Plugin plugin = this.plugins.get(mvnProject);
		if (plugin == null) {
			this.plugins.put(mvnProject, plugin = asPlugin());
		}
		return plugin;
	}

	@Override
	public boolean locatePluginDescriptor(final MavenPluginManager manager, final RepositorySystemSession session,
			final MavenProject mvnProject) throws InvalidPluginDescriptorException {
		try {
			setPluginDescriptor(mvnProject, manager.getPluginDescriptor(asPlugin(mvnProject),
					mvnProject.getRemotePluginRepositories(), session));
			return true;
		} catch (PluginResolutionException | PluginDescriptorParsingException e) {
			return false;
		}
	}

}
