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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import net.runeduniverse.tools.maven.r4m.grm.model.ModelUtils;
import net.runeduniverse.tools.maven.r4m.indexer.AProjectBoundEntry;

public class DefaultGrmArchiveSector extends AProjectBoundEntry<GoalRequirementArchiveSector>
		implements GoalRequirementArchiveSector {

	protected final Map<MergeDataGroup, GoalRequirementModel> origins = new LinkedHashMap<>();
	protected final Map<String, Map<GoalRequirementSource, Set<MergeDataGroup>>> matchBefore = new LinkedHashMap<>();
	protected final Map<String, Map<GoalRequirementSource, Set<MergeDataGroup>>> matchAfter = new LinkedHashMap<>();

	public DefaultGrmArchiveSector(final MavenProject mvnProject, final DefaultGrmArchiveSector parent) {
		super(mvnProject, parent);
	}

	@Override
	public GoalRequirementModel getModel(MergeDataGroup mergeGroup) {
		return this.origins.get(mergeGroup);
	}

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
		final List<String> keys = new LinkedList<>();
		keys.addAll(this.matchBefore.keySet());
		keys.addAll(this.matchAfter.keySet());
		keys.sort(null);

		CompoundTree keyTree = null;
		CompoundTree subTree = null;
		for (String key : keys) {
			tree.append(keyTree = new CompoundTree(key));
			keyTree.append(subTree = new CompoundTree("matchBefore"));
			appendEntries(subTree, this.matchBefore.get(key));
			keyTree.append(subTree = new CompoundTree("matchAfter"));
			appendEntries(subTree, this.matchAfter.get(key));
		}

		return tree;
	}

	protected void appendEntries(final CompoundTree tree,
			final Map<GoalRequirementSource, Set<MergeDataGroup>> entries) {
		if (tree == null || entries == null)
			return;

		final Map<GoalRequirementSource, CompoundTree> userMap = new LinkedHashMap<>();
		final Map<GoalRequirementSource, CompoundTree> projectMap = new LinkedHashMap<>();

		for (Entry<GoalRequirementSource, Set<MergeDataGroup>> entry : entries.entrySet()) {
			final GoalRequirementSource source = entry.getKey();
			final Set<MergeDataGroup> set = entry.getValue();
			if (set == null)
				continue;

			for (MergeDataGroup mergeGroup : set) {
				final GoalRequirementModel model = getModel(mergeGroup);
				final Map<GoalRequirementSource, CompoundTree> map = model.isEffective() ? userMap : projectMap;

				CompoundTree sourceTree = map.get(source);
				if (sourceTree == null)
					map.put(source,
							sourceTree = new CompoundTree("source", source == null ? "< null >" : source.key()));

				sourceTree.append(ModelUtils.toRecord(mergeGroup));
			}
		}

		CompoundTree subTree = new CompoundTree("origin", "user-defined");
		for (CompoundTree treeEntry : userMap.values()) {
			subTree.append(treeEntry);
		}
		tree.append(subTree);

		subTree = new CompoundTree("origin", "project-defined");
		for (CompoundTree treeEntry : projectMap.values()) {
			subTree.append(treeEntry);
		}
		tree.append(subTree);
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
