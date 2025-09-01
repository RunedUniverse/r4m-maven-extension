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
import java.util.function.Supplier;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;

public interface ModelSource extends Recordable {

	public String getProjectId();

	public String getPluginId();

	public Path getFile();

	public String getNote();

	public ModelSource setProjectId(String id);

	public ModelSource setPluginId(String id);

	public ModelSource setFile(Path file);

	public ModelSource setNote(String note);

	@Override
	public default CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("Source");

		final String project = getProjectId();
		if (project != null)
			tree.append("project", project);

		final String plugin = getPluginId();
		if (plugin != null)
			tree.append("plugin", plugin);

		final Path file = getFile();
		if (file != null)
			tree.append("file", file.toString());

		final String note = getNote();
		if (note != null)
			tree.append("note", note);

		return tree;
	}

	public static String id(final String groupId, final String artifactId) {
		return "" + groupId + ':' + artifactId;
	}

	public static String id(final Supplier<String> groupId, final Supplier<String> artifactId) {
		return id(groupId == null ? null : groupId.get(), artifactId == null ? null : artifactId.get());
	}

}
