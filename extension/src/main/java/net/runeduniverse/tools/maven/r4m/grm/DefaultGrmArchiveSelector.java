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

import java.util.Iterator;
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
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementCombineMethod;
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

	protected void merge(final Map<GoalData, Set<DataGroup>> keyIndex,
			final Map<DataGroup, Set<MergeDataGroup>> baseMap, final Map<DataGroup, Set<MergeDataGroup>> domMap,
			final GoalRequirementCombineMethod forceMethod) {
		for (Entry<DataGroup, Set<MergeDataGroup>> entry : domMap.entrySet()) {
			// evaluate combination method
			final Set<MergeDataGroup> remove = new LinkedHashSet<>();
			final Set<MergeDataGroup> append = new LinkedHashSet<>();

			for (MergeDataGroup value : entry.getValue()) {
				final GoalRequirementCombineMethod method = forceMethod == null ? getCombineMethod(value) : forceMethod;
				if (method == null)
					continue;
				if (GoalRequirementCombineMethod.REMOVE.equals(method)) {
					remove.add(value);
				} else if (GoalRequirementCombineMethod.REPLACE.equals(method)) {
					remove.add(value);
					append.add(value);
				} else if (GoalRequirementCombineMethod.APPEND.equals(method)) {
					append.add(value);
				}
			}
			// get all keys of the same goal
			final Set<DataGroup> keys = keyIndex.getOrDefault(findGoalData(entry.getKey()), new LinkedHashSet<>());

			keys.retainAll(baseMap.keySet());
			if (keys.isEmpty())
				keys.add(entry.getKey());

			// apply method
			for (DataGroup keyData : keys) {
				final Set<MergeDataGroup> baseSet = baseMap.computeIfAbsent(keyData, k -> new LinkedHashSet<>());

				if (!baseSet.isEmpty() && !remove.isEmpty())
					for (Iterator<MergeDataGroup> i = baseSet.iterator(); i.hasNext();) {
						final MergeDataGroup base = i.next();
						final GoalData baseGoal = findGoalData(base);
						final boolean required = base.isRequired();

						for (MergeDataGroup g : remove) {
							if (baseGoal.equals(findGoalData(g)) && required == g.isRequired())
								i.remove();
						}
					}
				baseSet.addAll(append);
			}
		}
	}

	protected GoalRequirementCombineMethod getCombineMethod(final MergeDataGroup data) {
		if (data == null)
			return null;
		GoalRequirementCombineMethod method = data.getCombineMethod();
		if (GoalRequirementCombineMethod.DEFAULT.equals(method)) {
			return data.getSource()
					.defaultCombineMethod();
		}
		return method;
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

		// get PACKAGING entries as base
		Map<DataGroup, Set<MergeDataGroup>> baseBefore = filterBySource(before, GoalRequirementSource.PACKAGING);
		Map<DataGroup, Set<MergeDataGroup>> baseAfter = filterBySource(after, GoalRequirementSource.PACKAGING);
		// index base goals
		indexGoalRefs(keyIndex, baseBefore);
		indexGoalRefs(keyIndex, baseAfter);
		// get WORKFLOW entries as dominant
		domBefore = filterBySource(before, GoalRequirementSource.WORKFLOW);
		domAfter = filterBySource(after, GoalRequirementSource.WORKFLOW);
		// merge GRM = WORKFLOW -> PACKAGING
		merge(keyIndex, baseBefore, domBefore, null);
		merge(keyIndex, baseAfter, domAfter, null);
		// mark entries base entries as dominant
		domBefore = baseBefore;
		domAfter = baseAfter;
		// get PLUGIN entries as base
		baseBefore = filterBySource(before, GoalRequirementSource.PLUGIN);
		baseAfter = filterBySource(after, GoalRequirementSource.PLUGIN);
		// clear index & index new batch of entries
		keyIndex.clear();
		indexGoalRefs(keyIndex, baseBefore);
		indexGoalRefs(keyIndex, baseAfter);
		// merge GRM = GRM -> PLUGIN
		merge(keyIndex, baseBefore, domBefore, GoalRequirementCombineMethod.REPLACE);
		merge(keyIndex, baseAfter, domAfter, GoalRequirementCombineMethod.REPLACE);
		// index dominant batch of entries
		indexGoalRefs(keyIndex, domBefore);
		indexGoalRefs(keyIndex, domAfter);
		// get OVERRIDE entries as dominant
		domBefore = filterBySource(before, GoalRequirementSource.OVERRIDE);
		domAfter = filterBySource(after, GoalRequirementSource.OVERRIDE);
		// merge GRM = OVERRIDE -> GRM
		merge(keyIndex, baseBefore, domBefore, null);
		merge(keyIndex, baseAfter, domAfter, null);

		// done => effective sources override everything!
		// TODO implement conversion
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
		final Set<DataGroup> set = index.computeIfAbsent(key, k -> new LinkedHashSet<>());
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
			final Set<MergeDataGroup> set = base.computeIfAbsent(entry.getKey()
					.copy(), k -> new LinkedHashSet<>());
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
