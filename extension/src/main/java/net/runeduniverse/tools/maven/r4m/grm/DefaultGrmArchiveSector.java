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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.maven3.ext.indexer.AProjectBoundEntry;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSector;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.DataGroup;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalContainer;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalData;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementModel;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementSource;
import net.runeduniverse.tools.maven.r4m.grm.model.MergeDataGroup;
import net.runeduniverse.tools.maven.r4m.grm.model.ModelUtils;

public class DefaultGrmArchiveSector extends AProjectBoundEntry<GoalRequirementArchiveSector>
		implements GoalRequirementArchiveSector {

	protected final Map<MergeDataGroup, GoalRequirementModel> origins = new LinkedHashMap<>();
	protected final Map<DataGroup, Map<GoalRequirementSource, Set<MergeDataGroup>>> matchBefore = new LinkedHashMap<>();
	protected final Map<DataGroup, Map<GoalRequirementSource, Set<MergeDataGroup>>> matchAfter = new LinkedHashMap<>();

	public DefaultGrmArchiveSector(final MavenProject mvnProject, final DefaultGrmArchiveSector parent) {
		super(mvnProject, parent);
	}

	@Override
	public GoalRequirementModel getModel(final MergeDataGroup mergeGroup) {
		return this.origins.get(mergeGroup);
	}

	@Override
	public Map<DataGroup, Set<MergeDataGroup>> getBeforeMatches() {
		return collectEntries(this.matchBefore, false);
	}

	@Override
	public Map<DataGroup, Set<MergeDataGroup>> getUserDefinedBeforeMatches() {
		return collectEntries(this.matchBefore, true);
	}

	@Override
	public Map<DataGroup, Set<MergeDataGroup>> getAfterMatches() {
		return collectEntries(this.matchAfter, false);
	}

	@Override
	public Map<DataGroup, Set<MergeDataGroup>> getUserDefinedAfterMatches() {
		return collectEntries(this.matchAfter, true);
	}

	protected Map<DataGroup, Set<MergeDataGroup>> collectEntries(
			final Map<DataGroup, Map<GoalRequirementSource, Set<MergeDataGroup>>> matchMap,
			final boolean requireUserDefined) {
		final Map<DataGroup, Set<MergeDataGroup>> map = new LinkedHashMap<>();
		for (Entry<DataGroup, Map<GoalRequirementSource, Set<MergeDataGroup>>> entry : matchMap.entrySet()) {
			final Set<MergeDataGroup> set = new LinkedHashSet<>();
			for (Entry<GoalRequirementSource, Set<MergeDataGroup>> sourceEntry : entry.getValue()
					.entrySet()) {
				for (MergeDataGroup data : sourceEntry.getValue()) {
					// check for user-defined flag
					if (requireUserDefined && !this.origins.get(data)
							.isUserDefined())
						continue;
					// collect data
					set.add(data);
				}
			}
			if (!set.isEmpty())
				map.put(entry.getKey(), set);
		}
		return map;
	}

	@Override
	public void register(final GoalRequirementModel grm) {
		if (grm == null)
			return;

		for (GoalContainer container : grm.getGoalContainer()) {
			final DataGroup keyData = container.getMatchGroup();

			indexEntries(matchBefore, keyData, grm, container.getPrerequisiteEntries());
			indexEntries(matchAfter, keyData, grm, container.getDependentEntries());
		}
	}

	protected void indexEntries(final Map<DataGroup, Map<GoalRequirementSource, Set<MergeDataGroup>>> map,
			final DataGroup key, final GoalRequirementModel grm, final Collection<DataEntry> entries) {
		for (DataEntry entry : entries) {
			if (!(entry instanceof MergeDataGroup))
				continue;
			final MergeDataGroup data = (MergeDataGroup) entry;
			final GoalRequirementSource source = data.getSource();

			this.origins.put(data, grm);

			final Map<GoalRequirementSource, Set<MergeDataGroup>> keyedMap = map.computeIfAbsent(key,
					k -> new LinkedHashMap<>());
			final Set<MergeDataGroup> set = keyedMap.computeIfAbsent(source, k -> new LinkedHashSet<>());

			set.add(data);
		}
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = super.toRecord();
		final Set<DataGroup> keys = new LinkedHashSet<>();
		keys.addAll(this.matchBefore.keySet());
		keys.addAll(this.matchAfter.keySet());
		final List<DataGroup> keyList = new LinkedList<>(keys);
		keyList.sort(this::compareGoalDataByArtifactKey);

		CompoundTree keyTree = null;
		CompoundTree subTree = null;
		for (DataGroup key : keyList) {
			tree.append(keyTree = new DefaultCompoundTree("--- Goal Matches ---"));
			keyTree.append(subTree = new DefaultCompoundTree("» item"));
			subTree.append(ModelUtils.toRecord(key));
			keyTree.append(subTree = new DefaultCompoundTree("» before"));
			appendEntries(subTree, this.matchBefore.get(key));
			keyTree.append(subTree = new DefaultCompoundTree("» after"));
			appendEntries(subTree, this.matchAfter.get(key));
		}

		return tree;
	}

	protected int compareGoalDataByArtifactKey(final DataGroup d0, final DataGroup d1) {
		final String s0 = createKey(d0);
		final String s1 = createKey(d1);
		if (s0 == null) {
			if (s1 == null)
				return 0;
			else
				return -1;
		}
		if (s1 == null)
			return 1;
		return s0.compareTo(s1);
	}

	protected String createKey(final DataGroup data) {
		if (data == null)
			return null;
		String groupId = null;
		String artifactId = null;
		String goalId = null;
		for (DataEntry entry : data.getEntries()) {
			if (entry == null)
				continue;
			if (entry instanceof GoalData) {
				final GoalData gData = (GoalData) entry;
				groupId = gData.getGroupId();
				artifactId = gData.getArtifactId();
				goalId = gData.getGoalId();
				break;
			}
		}
		if (groupId == null || artifactId == null || goalId == null)
			return null;
		return String.join(":", groupId, artifactId, goalId);
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
				final Map<GoalRequirementSource, CompoundTree> map = model.isUserDefined() ? userMap : projectMap;
				final CompoundTree sourceTree = map.computeIfAbsent(source,
						k -> new DefaultCompoundTree("source", k == null ? "< null >" : k.key()));

				sourceTree.append(ModelUtils.toRecord(mergeGroup));
			}
		}

		CompoundTree subTree = new DefaultCompoundTree("origin", "user-defined");
		for (CompoundTree treeEntry : userMap.values()) {
			subTree.append(treeEntry);
		}
		tree.append(subTree);

		subTree = new DefaultCompoundTree("origin", "project-defined");
		for (CompoundTree treeEntry : projectMap.values()) {
			subTree.append(treeEntry);
		}
		tree.append(subTree);
	}

	@Override
	protected String _getRecordTitle() {
		return "GRM ArchiveSector";
	}
}
