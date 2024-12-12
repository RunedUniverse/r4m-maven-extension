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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.lib.utils.conditions.tools.ConditionIndexer;
import net.runeduniverse.lib.utils.conditions.tools.EntrySet;
import net.runeduniverse.tools.maven.r4m.api.Settings;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchive;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSector;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSelection;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSelector;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.DataGroup;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalData;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementSource;
import net.runeduniverse.tools.maven.r4m.grm.model.MergeDataGroup;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;

@Component(role = GoalRequirementArchiveSelector.class, hint = "default", instantiationStrategy = "singleton")
public class DefaultGrmArchiveSelector implements GoalRequirementArchiveSelector {

	@Requirement
	private Logger log;
	@Requirement
	private Settings settings;
	@Requirement
	private GoalRequirementArchive archive;

	// if grm is user-defined than it is inherited
	// as long as no user-defined grm was found,
	// any other grm is loaded from the plugins in the active project!
	protected Map<DataGroup, Set<MergeDataGroup>> collectMatches(final GoalRequirementArchiveSector sector,
			final MatchGetter getter, final EffectiveMatchGetter effGetter) {
		final Map<DataGroup, Set<MergeDataGroup>> map = new LinkedHashMap<>();
		copyToMap(map, getter.get(sector));

		Map<DataGroup, Set<MergeDataGroup>> userDefined = effGetter.get(sector);
		// skip as the values are already included!
		if (!userDefined.isEmpty())
			return map;

		for (GoalRequirementArchiveSector parent = sector.getParent(); userDefined.isEmpty()
				&& parent != null; parent = parent.getParent()) {
			userDefined = effGetter.get(parent);
		}
		if (!userDefined.isEmpty()) {
			copyToMap(map, userDefined);
		}
		return map;
	}

	protected void merge(Map<DataGroup, Set<MergeDataGroup>> base, Map<DataGroup, Set<MergeDataGroup>> dom) {

	}

	@SuppressWarnings("deprecation")
	protected void getChecks(final GoalRequirementArchiveSector sector, final EntrySet<EntityView> set) {
		// TODO collect data & compile conditions

		final Map<DataGroup, Set<MergeDataGroup>> before = collectMatches(sector, //
				GoalRequirementArchiveSector::getBeforeMatches, //
				GoalRequirementArchiveSector::getEffectiveBeforeMatches);
		final Map<DataGroup, Set<MergeDataGroup>> after = collectMatches(sector, //
				GoalRequirementArchiveSector::getAfterMatches, //
				GoalRequirementArchiveSector::getEffectiveAfterMatches);

		Map<DataGroup, Set<MergeDataGroup>> domBefore;
		Map<DataGroup, Set<MergeDataGroup>> domAfter;

		// check for effective
		domBefore = filterBySource(before, GoalRequirementSource.EFFECTIVE);
		domAfter = filterBySource(after, GoalRequirementSource.EFFECTIVE);
		if (!domBefore.isEmpty() || !domAfter.isEmpty()) {
			// done => effective sources override everything!
			// TODO implement conversion
			return;
		}

		final Map<GoalData, Set<DataGroup>> keyIndex = new LinkedHashMap<>();

		Map<DataGroup, Set<MergeDataGroup>> baseBefore = filterBySource(before, GoalRequirementSource.PACKAGING);
		Map<DataGroup, Set<MergeDataGroup>> baseAfter = filterBySource(after, GoalRequirementSource.PACKAGING);

		indexGoalRefs(keyIndex, baseBefore);
		indexGoalRefs(keyIndex, baseAfter);

		domBefore = filterBySource(before, GoalRequirementSource.WORKFLOW);
		domAfter = filterBySource(after, GoalRequirementSource.WORKFLOW);

	}

	protected void indexGoalRefs(final Map<GoalData, Set<DataGroup>> index,
			final Map<DataGroup, Set<MergeDataGroup>> map) {

		for (DataGroup group : map.keySet()) {
			indexGoalRef(index, group);
		}

		for (Set<MergeDataGroup> valueSet : map.values()) {
			if (valueSet == null || valueSet.isEmpty())
				continue;
			for (MergeDataGroup group : valueSet)
				indexGoalRef(index, group);
		}
	}

	protected void indexGoalRef(final Map<GoalData, Set<DataGroup>> index, final DataGroup group) {
		final GoalData key = findGoalData(group);
		if (key == null)
			return;
		Set<DataGroup> set = index.get(key);
		if (set == null)
			index.put(key, set = new LinkedHashSet<>());
		set.add(group);
	}

	@Override
	public GoalRequirementArchiveSelection compileSelection(final GoalRequirementArchiveSelectorConfig selectorConfig) {
		final MavenProject mvnProject = selectorConfig.getActiveProject();
		final EntrySet<EntityView> set = new EntrySet<>();
		if (mvnProject == null)
			return new DefaultGrmArchiveSelection(selectorConfig.clone(), set);

		final GoalRequirementArchiveSector sector = this.archive.getSector(mvnProject);
		if (sector == null)
			return new DefaultGrmArchiveSelection(selectorConfig.clone(), set);

		getChecks(sector, set);
		set.compile(new ConditionIndexer());

		return new DefaultGrmArchiveSelection(selectorConfig.clone(), set);
	}

	protected static void copyToMap(final Map<DataGroup, Set<MergeDataGroup>> base,
			final Map<DataGroup, Set<MergeDataGroup>> add) {
		for (Entry<DataGroup, Set<MergeDataGroup>> entry : add.entrySet()) {
			final Set<MergeDataGroup> src = entry.getValue();
			if (src == null || src.isEmpty())
				continue;
			Set<MergeDataGroup> set = base.get(entry.getKey());
			if (set == null)
				base.put(entry.getKey()
						.copy(), set = new LinkedHashSet<>());
			for (MergeDataGroup data : src)
				set.add(data.copy());
		}
	}

	protected static Map<DataGroup, Set<MergeDataGroup>> filterBySource(final Map<DataGroup, Set<MergeDataGroup>> input,
			final GoalRequirementSource source) {
		final Map<DataGroup, Set<MergeDataGroup>> map = new LinkedHashMap<>();
		if (source == null)
			return map;
		for (Entry<DataGroup, Set<MergeDataGroup>> entry : input.entrySet()) {
			final Set<MergeDataGroup> set = new LinkedHashSet<>();
			for (MergeDataGroup group : entry.getValue()) {
				if (source.equals(group.getSource()))
					set.add(group);
			}
			if (set.isEmpty())
				continue;
			map.put(entry.getKey(), set);
		}
		return map;
	}

	protected static GoalData findGoalData(final DataGroup group) {
		for (DataEntry entry : group.getEntries()) {
			if (entry instanceof GoalData)
				return (GoalData) entry;
		}
		return null;
	}

	@FunctionalInterface
	protected interface MatchGetter {

		public Map<DataGroup, Set<MergeDataGroup>> get(final GoalRequirementArchiveSector sector);

	}

	@FunctionalInterface
	protected interface EffectiveMatchGetter {

		public Map<DataGroup, Set<MergeDataGroup>> get(final GoalRequirementArchiveSector sector);

	}
}
