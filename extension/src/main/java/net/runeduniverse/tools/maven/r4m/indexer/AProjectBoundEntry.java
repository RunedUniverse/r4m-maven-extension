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

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;
import net.runeduniverse.tools.maven.r4m.indexer.api.ProjectBoundEntry;

public class AProjectBoundEntry<E extends ProjectBoundEntry<E>> implements ProjectBoundEntry<E>, Recordable {

	protected final MavenProject mvnProject;
	protected E parent;

	protected AProjectBoundEntry(final MavenProject mvnProject, final E parent) {
		this.mvnProject = mvnProject;
		this.parent = parent;
	}

	@Override
	public MavenProject getMvnProject() {
		return this.mvnProject;
	}

	@Override
	public E getParent() {
		return this.parent;
	}

	@Override
	public void setParent(E parent) {
		this.parent = parent;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new CompoundTree(_getRecordTitle());

		tree.append("project id", this.mvnProject.getId());

		if (this.parent != null)
			tree.append("parent project id", this.parent.getMvnProject()
					.getId());

		return tree;
	}

	protected String _getRecordTitle() {
		return getClass().getCanonicalName();
	}
}
