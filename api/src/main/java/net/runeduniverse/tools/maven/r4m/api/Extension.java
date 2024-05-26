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
package net.runeduniverse.tools.maven.r4m.api;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.aether.RepositorySystemSession;

public interface Extension {

	public ClassRealm getClassRealm();

	public String getGroupId();

	public String getArtifactId();

	public String getVersion();

	public String getId();

	public boolean isPlugin(final MavenProject mvnProject);

	public PluginDescriptor getPluginDescriptor(final MavenProject mvnProject);

	public void setClassRealm(ClassRealm realm);

	public void setGroupId(String groupId);

	public void setArtifactId(String artifactId);

	public void setVersion(String version);

	public void setPlugin(Plugin plugin);

	public void setPlugin(final MavenProject mvnProject, Plugin plugin);

	public void setPluginDescriptor(final MavenProject mvnProject, PluginDescriptor descriptor);

	public Plugin asPlugin();

	public Plugin asPlugin(final MavenProject mvnProject);

	/**
	 * Tries to locate the {@link PluginDescriptor} for the provided
	 * {@link MavenProject}, if found it will be set.
	 *
	 * @param manager
	 * @param session
	 * @param mvnProject
	 * @return {@code true} if the {@link PluginDescriptor} for the provided
	 *         {@link MavenProject} was located
	 * @throws InvalidPluginDescriptorException
	 */
	public boolean locatePluginDescriptor(final MavenPluginManager manager, final RepositorySystemSession session,
			final MavenProject mvnProject) throws InvalidPluginDescriptorException;

}
