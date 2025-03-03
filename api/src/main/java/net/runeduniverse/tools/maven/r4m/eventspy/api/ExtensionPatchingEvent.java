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
package net.runeduniverse.tools.maven.r4m.eventspy.api;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.maven.ext.data.api.Extension;

public interface ExtensionPatchingEvent extends PatchingEvent {

	public Set<MavenProject> getProjects();

	public Set<Extension> getExtensions();

	public static ExtensionEvent createInfoEvent(Type type) {
		return new ExtensionEvent(type, null);
	}

	public static ExtensionEvent createInfoEvent(Type type, Collection<MavenProject> projects,
			Collection<Extension> extensions) {
		return new ExtensionEvent(type, null, projects, extensions);
	}

	public static ExtensionEvent createErrorEvent(Type type, Exception exception) {
		return new ExtensionEvent(type, exception);
	}

	public static ExtensionEvent createErrorEvent(Type type, Exception exception, Collection<MavenProject> projects,
			Collection<Extension> extensions) {
		return new ExtensionEvent(type, exception, projects, extensions);
	}

	public class ExtensionEvent extends PatchingEvent.BasicEvent implements ExtensionPatchingEvent {

		protected Set<MavenProject> projects = new LinkedHashSet<>();
		protected Set<Extension> extensions = new LinkedHashSet<>();

		public ExtensionEvent(Type type, Exception exception) {
			super(type, exception);
		}

		public ExtensionEvent(Type type, Exception exception, Collection<MavenProject> projects,
				Collection<Extension> extensions) {
			super(type, exception);
			this.projects.addAll(projects);
			this.extensions.addAll(extensions);
		}

		public ExtensionEvent readonly() {
			this.projects = Collections.unmodifiableSet(this.projects);
			this.extensions = Collections.unmodifiableSet(this.extensions);
			return this;
		}

		@Override
		public Set<MavenProject> getProjects() {
			return this.projects;
		}

		@Override
		public Set<Extension> getExtensions() {
			return this.extensions;
		}

	}

}
