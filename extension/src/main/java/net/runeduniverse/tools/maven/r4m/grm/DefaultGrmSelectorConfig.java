/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.grm;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSelectorConfig;

public class DefaultGrmSelectorConfig implements GoalRequirementArchiveSelectorConfig {

	private MavenProject mvnProject = null;

	@Override
	public GoalRequirementArchiveSelectorConfig selectActiveProject(final MavenProject value) {
		this.mvnProject = value;
		return this;
	}

	@Override
	public GoalRequirementArchiveSelectorConfig clearActiveProject() {
		this.mvnProject = null;
		return this;
	}

	@Override
	public MavenProject getActiveProject() {
		return this.mvnProject;
	}

	@Override
	public GoalRequirementArchiveSelectorConfig clone() {
		final DefaultGrmSelectorConfig cnf = new DefaultGrmSelectorConfig();
		cnf.selectActiveProject(this.mvnProject);
		return cnf;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("GRM Selector Config");

		if (this.mvnProject != null)
			tree.append(new DefaultCompoundTree("Maven Project").append("groupId", this.mvnProject.getGroupId())
					.append("artifactId", this.mvnProject.getArtifactId())
					.append("version", this.mvnProject.getVersion()));

		return tree;
	}
}
