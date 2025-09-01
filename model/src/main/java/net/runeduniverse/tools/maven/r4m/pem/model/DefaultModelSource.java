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
package net.runeduniverse.tools.maven.r4m.pem.model;

import java.nio.file.Path;

import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;

public class DefaultModelSource implements ModelSource {

	protected String projectId = null;
	protected String pluginId = null;
	protected Path file = null;
	protected String note = null;

	public DefaultModelSource() {
	}

	@Override
	public String getProjectId() {
		return this.projectId;
	}

	@Override
	public String getPluginId() {
		return this.pluginId;
	}

	@Override
	public Path getFile() {
		return this.file;
	}

	@Override
	public String getNote() {
		return this.note;
	}

	@Override
	public ModelSource setProjectId(final String id) {
		this.projectId = id;
		return this;
	}

	@Override
	public ModelSource setPluginId(final String id) {
		this.pluginId = id;
		return this;
	}

	@Override
	public ModelSource setFile(final Path file) {
		this.file = file;
		return this;
	}

	@Override
	public ModelSource setNote(String note) {
		this.note = note;
		return this;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof ModelSource))
			return false;
		final ModelSource other = (ModelSource) obj;

		return objectEquals(getProjectId(), other.getProjectId()) //
				&& objectEquals(getPluginId(), other.getPluginId()) //
				&& objectEquals(getFile(), other.getFile()) //
				&& objectEquals(getNote(), other.getNote());
	}
}
