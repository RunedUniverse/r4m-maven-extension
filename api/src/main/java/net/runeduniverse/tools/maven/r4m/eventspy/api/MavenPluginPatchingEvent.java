/*
 * Copyright © 2023 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.eventspy.api;

import java.util.Collection;

import org.apache.maven.model.Plugin;

public interface MavenPluginPatchingEvent extends PatchingEvent {

	public Collection<Plugin> getEffectedPlugins();

	public static MavenPluginEvent createInfoEvent(Type type, Collection<Plugin> effectedPlugins) {
		return new MavenPluginEvent(type, null, effectedPlugins);
	}

	public static MavenPluginEvent createErrorEvent(Type type, Exception exception,
			Collection<Plugin> effectedPlugins) {
		return new MavenPluginEvent(type, exception, effectedPlugins);
	}

	public class MavenPluginEvent extends PatchingEvent.BasicEvent implements MavenPluginPatchingEvent {

		protected Collection<Plugin> effectedPlugins = null;

		public MavenPluginEvent(Type type, Exception exception, Collection<Plugin> effectedPlugins) {
			super(type, exception);
			this.effectedPlugins = effectedPlugins;
		}

		@Override
		public Collection<Plugin> getEffectedPlugins() {
			return effectedPlugins;
		}

	}

}
