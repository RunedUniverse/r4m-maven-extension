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
package net.runeduniverse.tools.maven.r4m.grm;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.lib.utils.maven.ext.indexer.AProjectBoundArchive;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchive;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSector;

@Component(role = GoalRequirementArchive.class, instantiationStrategy = "keep-alive")
public class DefaultGrmArchive extends AProjectBoundArchive<GoalRequirementArchiveSector>
		implements GoalRequirementArchive {

	@Override
	protected GoalRequirementArchiveSector _newSector(final MavenProject mvnProject) {
		return new DefaultGrmArchiveSector(mvnProject, null);
	}

	@Override
	protected void _updateSector(final GoalRequirementArchiveSector child, final GoalRequirementArchiveSector parent) {
		if (child == null)
			return;
		child.setParent(parent);
	}

	@Override
	protected String _getRecordTitle() {
		return "GoalRequirementArchive";
	}
}
