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

import java.util.Comparator;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.lib.utils.conditions.tools.ConditionComparator;
import net.runeduniverse.lib.utils.conditions.tools.ConditionIndexer;
import net.runeduniverse.lib.utils.conditions.tools.EntrySet;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchive;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSector;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;
import net.runeduniverse.tools.maven.r4m.indexer.AProjectBoundArchive;

@Component(role = GoalRequirementArchive.class, instantiationStrategy = "keep-alive")
public class DefaultGrmArchive extends AProjectBoundArchive<GoalRequirementArchiveSector>
		implements GoalRequirementArchive {

	public Comparator<EntityView> getComparator() {
		// TODO implement
		final EntrySet<EntityView> set = new EntrySet<>();
		set.compile(new ConditionIndexer());
		return new ConditionComparator<EntityView>(set);
	}

	@Override
	protected GoalRequirementArchiveSector _newSector(MavenProject mvnProject) {
		return new DefaultGrmArchiveSector(mvnProject, null);
	}

	@Override
	protected void _updateSector(GoalRequirementArchiveSector parent, GoalRequirementArchiveSector child) {
		if (child == null)
			return;
		child.setParent(parent);
	}

	@Override
	protected String _getRecordTitle() {
		return "GoalRequirementArchive";
	}
}
