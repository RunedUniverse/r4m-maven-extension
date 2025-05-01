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
package net.runeduniverse.tools.maven.r4m.eventspy.api;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.common.CollectionUtils;
import net.runeduniverse.lib.utils.maven3.ext.data.api.Extension;

public interface ExtensionPatchingEvent extends PatchingEvent {

	public Set<MavenProject> getProjects();

	public Set<Extension> getExtensions();

	public static ExtensionEvent createInfoEvent(final Type type) {
		return new ExtensionEvent(type, null);
	}

	public static ExtensionEvent createInfoEvent(final Type type, final Collection<MavenProject> projects,
			Collection<Extension> extensions) {
		return new ExtensionEvent(type, null, projects, extensions);
	}

	public static ExtensionEvent createErrorEvent(final Type type, final Exception exception) {
		return new ExtensionEvent(type, exception);
	}

	public static ExtensionEvent createErrorEvent(final Type type, final Exception exception,
			final Collection<MavenProject> projects, Collection<Extension> extensions) {
		return new ExtensionEvent(type, exception, projects, extensions);
	}

	public class ExtensionEvent extends PatchingEvent.BasicEvent implements ExtensionPatchingEvent {

		protected Set<MavenProject> projects;
		protected Set<Extension> extensions;

		protected ExtensionEvent(final Type type, final Exception exception, final Set<MavenProject> projects,
				final Set<Extension> extensions) {
			super(type, exception);
			this.projects = projects;
			this.extensions = extensions;
		}

		public ExtensionEvent(final Type type, final Exception exception) {
			this(type, exception, new LinkedHashSet<>(), new LinkedHashSet<>());
		}

		public ExtensionEvent(final Type type, final Exception exception, final Collection<MavenProject> projects,
				final Collection<Extension> extensions) {
			this(type, exception, new LinkedHashSet<>(), new LinkedHashSet<>());
			this.projects.addAll(projects);
			this.extensions.addAll(extensions);
		}

		public ExtensionEvent readonly() {
			this.projects = CollectionUtils.unmodifiable(this.projects);
			this.extensions = CollectionUtils.unmodifiable(this.extensions);
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
