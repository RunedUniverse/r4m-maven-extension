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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSector;
import net.runeduniverse.tools.maven.r4m.grm.model.ArtifactIdData;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalContainer;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalIdData;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementModel;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementSource;
import net.runeduniverse.tools.maven.r4m.grm.model.GroupIdData;
import net.runeduniverse.tools.maven.r4m.grm.model.MergeDataGroup;
import net.runeduniverse.tools.maven.r4m.indexer.AProjectBoundEntry;

public class DefaultGrmArchiveSector extends AProjectBoundEntry<GoalRequirementArchiveSector>
		implements GoalRequirementArchiveSector {

	protected final Map<MergeDataGroup, GoalRequirementModel> origins = new LinkedHashMap<>();
	protected final Map<String, Map<GoalRequirementSource, Set<MergeDataGroup>>> matchBefore = new LinkedHashMap<>();
	protected final Map<String, Map<GoalRequirementSource, Set<MergeDataGroup>>> matchAfter = new LinkedHashMap<>();

	public DefaultGrmArchiveSector(final MavenProject mvnProject, final DefaultGrmArchiveSector parent) {
		super(mvnProject, parent);
	}

	// TODO implement getModel()
	// @Override
	// public GoalRequirementModel getModel(Object sourcedModelEntry) {
	// }

	@Override
	public void register(final GoalRequirementModel grm) {
		if (grm == null)
			return;

		for (GoalContainer container : grm.getGoalContainer()) {
			final String key = createKey(container.getMatchEntries());
			indexEntries(matchBefore, key, grm, container.getPrerequisiteEntries());
			indexEntries(matchAfter, key, grm, container.getDependentEntries());
		}
	}

	protected void indexEntries(final Map<String, Map<GoalRequirementSource, Set<MergeDataGroup>>> map,
			final String key, final GoalRequirementModel grm, final Collection<DataEntry> entries) {
		for (DataEntry entry : entries) {
			if (!(entry instanceof MergeDataGroup))
				continue;
			final MergeDataGroup data = (MergeDataGroup) entry;
			final GoalRequirementSource source = data.getSource();

			this.origins.put(data, grm);

			Map<GoalRequirementSource, Set<MergeDataGroup>> keyedMap = map.get(key);
			if (keyedMap == null)
				map.put(key, keyedMap = new LinkedHashMap<>());

			Set<MergeDataGroup> set = keyedMap.get(source);
			if (set == null)
				keyedMap.put(source, set = new LinkedHashSet<>());

			set.add(data);
		}
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = super.toRecord();

		// TODO log entries
		/*
		 * for (Map<ExecutionSource, Set<Execution>> valuesBySource :
		 * this.executions.values()) for (Set<Execution> executions :
		 * valuesBySource.values()) for (Execution execution : executions)
		 * tree.append(execution.toRecord());
		 */

		return tree;
	}

	@Override
	protected String _getRecordTitle() {
		return "ArchiveSector";
	}

	protected String createKey(final Collection<DataEntry> entries) {
		String groupId = null;
		String artifactId = null;
		String goalId = null;
		for (DataEntry entry : entries) {
			if (entry == null)
				continue;
			if (entry instanceof GroupIdData)
				groupId = ((GroupIdData) entry).getGroupId();
			else if (entry instanceof ArtifactIdData)
				artifactId = ((ArtifactIdData) entry).getArtifactId();
			else if (entry instanceof GoalIdData)
				goalId = ((GoalIdData) entry).getGoalId();
		}
		return String.join(":", groupId, artifactId, goalId);
	}
}
