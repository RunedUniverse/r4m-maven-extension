/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.event.api;

import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

public interface MavenPluginPatchingEvent extends PatchingEvent {

	public Map<MavenProject, Set<Plugin>> getEffectedPluginsPerProject();

	public static MavenPluginEvent createInfoEvent(final Type type,
			final Map<MavenProject, Set<Plugin>> effectedPlugins) {
		return new MavenPluginEvent(type, null, effectedPlugins);
	}

	public static MavenPluginEvent createErrorEvent(final Type type, final Exception exception,
			final Map<MavenProject, Set<Plugin>> effectedPluginsPerProject) {
		return new MavenPluginEvent(type, exception, effectedPluginsPerProject);
	}

	public class MavenPluginEvent extends PatchingEvent.BasicEvent implements MavenPluginPatchingEvent {

		protected Map<MavenProject, Set<Plugin>> effectedPlugins = null;

		public MavenPluginEvent(final Type type, final Exception exception,
				final Map<MavenProject, Set<Plugin>> effectedPlugins) {
			super(type, exception);
			this.effectedPlugins = effectedPlugins;
		}

		@Override
		public Map<MavenProject, Set<Plugin>> getEffectedPluginsPerProject() {
			return this.effectedPlugins;
		}

	}

}
