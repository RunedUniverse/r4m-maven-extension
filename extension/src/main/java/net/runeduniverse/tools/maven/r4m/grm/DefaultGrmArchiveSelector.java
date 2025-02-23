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
import java.util.function.Supplier;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.lib.utils.conditions.api.Condition;
import net.runeduniverse.lib.utils.conditions.tools.ConditionIndexer;
import net.runeduniverse.lib.utils.conditions.tools.EntrySet;
import net.runeduniverse.tools.maven.r4m.api.Settings;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchive;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSector;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSelection;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSelector;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.grm.check.converter.api.CheckConverter;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.DataGroup;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalData;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementCombineMethod;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementSource;
import net.runeduniverse.tools.maven.r4m.grm.model.MergeDataGroup;
import net.runeduniverse.tools.maven.r4m.grm.model.WhenDataGroup;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;

@Component(role = GoalRequirementArchiveSelector.class, hint = "default", instantiationStrategy = "singleton")
public class DefaultGrmArchiveSelector implements GoalRequirementArchiveSelector {

	@Requirement
	private Logger log;
	@Requirement
	private Settings settings;
	@Requirement
	private GoalRequirementArchive archive;
	@Requirement
	private CheckConverter converter;

	// if grm is user-defined than it is inherited
	// as long as no user-defined grm was found,
	// any other grm is loaded from the plugins in the active project!
	protected Map<DataGroup, Set<MergeDataGroup>> collectMatches(final GoalRequirementArchiveSector sector,
			final MatchGetter getter, final MatchGetter effGetter) {
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

	protected void merge(final Map<DataGroup, Set<MergeDataGroup>> baseMap,
			final Map<DataGroup, Set<MergeDataGroup>> domMap, final GoalRequirementCombineMethod forceMethod) {
		// index base goals
		final Map<GoalData, Set<DataGroup>> keyIndex = new LinkedHashMap<>();
		for (DataGroup group : baseMap.keySet()) {
			indexGoalRef(keyIndex, group);
		}

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
			final DataGroup key = entry.getKey();
			final Set<DataGroup> keys = keyIndex.computeIfAbsent(findGoalData(key), k -> {
				// in case the key was not previously used -> add it
				final Set<DataGroup> set = new LinkedHashSet<>();
				set.add(key);
				return set;
			});

			// apply method
			for (DataGroup keyData : keys) {
				final Set<MergeDataGroup> baseSet = baseMap.computeIfAbsent(keyData, k -> new LinkedHashSet<>());

				if (!baseSet.isEmpty() && !remove.isEmpty())
					for (Iterator<MergeDataGroup> i = baseSet.iterator(); i.hasNext();) {
						final MergeDataGroup base = i.next();
						final GoalData baseGoal = findGoalData(base);
						final boolean required = base.getRequired();

						for (MergeDataGroup g : remove) {
							if (baseGoal.equals(findGoalData(g)) && required == g.getRequired()) {
								i.remove();
								break;
							}
						}
					}
				baseSet.addAll(append);
			}
		}
	}

	protected void reduce(final Map<DataGroup, Set<MergeDataGroup>> map,
			final Supplier<MergeDataGroup> reductionSupplier) {
		for (Iterator<Entry<DataGroup, Set<MergeDataGroup>>> i = map.entrySet()
				.iterator(); i.hasNext();) {
			final Entry<DataGroup, Set<MergeDataGroup>> entry = i.next();
			final Set<MergeDataGroup> set = entry.getValue();
			// empty sets are removed
			if (set == null || set.isEmpty()) {
				i.remove();
				continue;
			}
			// reduce per SOURCE = we still need it as priority later during evaluation to
			// resolve circular requirements!
			for (Entry<GoalRequirementSource, Set<MergeDataGroup>> srcEntry : categorizeBySource(set).entrySet()) {
				// reduce per targeted goal
				final Map<GoalData, Set<MergeDataGroup>> index = new LinkedHashMap<>();
				for (MergeDataGroup g : srcEntry.getValue()) {
					indexGoalRef(index, g);
				}
				for (Entry<GoalData, Set<MergeDataGroup>> e : index.entrySet()) {
					final Map<Boolean, WhenDataGroup> requiredMap = new LinkedHashMap<>();
					// reduce every entry
					for (MergeDataGroup g : e.getValue()) {
						// remove entry that gets reduced
						set.remove(g);
						// start reduction
						final WhenDataGroup when = findWhenDataGroup(g);
						final WhenDataGroup reducedWhen = requiredMap.computeIfAbsent(g.getRequired(),
								k -> new WhenDataGroup());

						if (when == null) {
							reducedWhen.setAlwaysActive(true);
							continue;
						}

						if (when.getAlwaysActive())
							reducedWhen.setAlwaysActive(true);
						if (when.getNeverActive())
							reducedWhen.setNeverActive(true);
						reducedWhen.getEntries()
								.addAll(when.getEntries());
					}
					// finalize reduction & add summarized entry to set
					for (Boolean b : new Boolean[] { Boolean.TRUE, Boolean.FALSE }) {
						final WhenDataGroup when = requiredMap.get(b);
						if (when == null)
							continue;
						final MergeDataGroup reduction = reductionSupplier.get();
						set.add(reduction);
						reduction.setSource(srcEntry.getKey());
						reduction.setRequired(b);
						reduction.addEntry(e.getKey()
								.copy());

						if (when.getAlwaysActive() || when.getNeverActive()) {
							// this automatically invalidates all other conditions!
							when.getEntries()
									.clear();
						}
						if (when.valid())
							reduction.addEntry(when);
					}
				}
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
			reduce(domBefore, reductionSupplierOfType(MergeDataGroup.MODEL_MATCH_BEFORE_TYPE));
			reduce(domAfter, reductionSupplierOfType(MergeDataGroup.MODEL_MATCH_AFTER_TYPE));
			// done => effective sources override everything!
			convertToEntrySet(domBefore, domAfter, set);
			return;
		}

		// get PACKAGING entries as base
		Map<DataGroup, Set<MergeDataGroup>> baseBefore = filterBySource(before, GoalRequirementSource.PACKAGING);
		Map<DataGroup, Set<MergeDataGroup>> baseAfter = filterBySource(after, GoalRequirementSource.PACKAGING);
		// get WORKFLOW entries as dominant
		domBefore = filterBySource(before, GoalRequirementSource.WORKFLOW);
		domAfter = filterBySource(after, GoalRequirementSource.WORKFLOW);
		// merge GRM = WORKFLOW -> PACKAGING
		merge(baseBefore, domBefore, null);
		merge(baseAfter, domAfter, null);
		// mark entries base entries as dominant
		domBefore = baseBefore;
		domAfter = baseAfter;
		// get PLUGIN entries as base
		baseBefore = filterBySource(before, GoalRequirementSource.PLUGIN);
		baseAfter = filterBySource(after, GoalRequirementSource.PLUGIN);
		// merge GRM = GRM -> PLUGIN
		merge(baseBefore, domBefore, GoalRequirementCombineMethod.REPLACE);
		merge(baseAfter, domAfter, GoalRequirementCombineMethod.REPLACE);
		// get OVERRIDE entries as dominant
		domBefore = filterBySource(before, GoalRequirementSource.OVERRIDE);
		domAfter = filterBySource(after, GoalRequirementSource.OVERRIDE);
		// merge GRM = OVERRIDE -> GRM
		merge(baseBefore, domBefore, null);
		merge(baseAfter, domAfter, null);

		reduce(baseBefore, reductionSupplierOfType(MergeDataGroup.MODEL_MATCH_BEFORE_TYPE));
		reduce(baseAfter, reductionSupplierOfType(MergeDataGroup.MODEL_MATCH_AFTER_TYPE));
		// done => effective sources override everything!
		convertToEntrySet(baseBefore, baseAfter, set);
	}

	protected void convertToEntrySet(final Map<DataGroup, Set<MergeDataGroup>> before,
			final Map<DataGroup, Set<MergeDataGroup>> after, final EntrySet<EntityView> set) {
		for (Entry<DataGroup, Set<MergeDataGroup>> entry : before.entrySet()) {
			final Condition<EntityView> match = this.converter.convertEntry(entry.getKey());
			for (MergeDataGroup group : entry.getValue()) {
				set.add(match, this.converter.convertEntry(group), null);
			}
		}
		for (Entry<DataGroup, Set<MergeDataGroup>> entry : after.entrySet()) {
			final Condition<EntityView> match = this.converter.convertEntry(entry.getKey());
			for (MergeDataGroup group : entry.getValue()) {
				set.add(match, null, this.converter.convertEntry(group));
			}
		}
	}

	protected <T extends DataGroup> void indexGoalRef(final Map<GoalData, Set<T>> index, final T group) {
		final GoalData key = findGoalData(group);
		if (key == null)
			return;
		final Set<T> set = index.computeIfAbsent(key, k -> new LinkedHashSet<>());
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

	protected static Supplier<MergeDataGroup> reductionSupplierOfType(final String type) {
		return new Supplier<MergeDataGroup>() {

			@Override
			public MergeDataGroup get() {
				return new MergeDataGroup(type);
			}
		};
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
		if (input == null || source == null)
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

	protected static Map<GoalRequirementSource, Set<MergeDataGroup>> categorizeBySource(
			final Set<MergeDataGroup> input) {
		final Map<GoalRequirementSource, Set<MergeDataGroup>> map = new LinkedHashMap<>();
		if (input == null)
			return map;
		for (MergeDataGroup group : input) {
			if (group == null)
				continue;
			final Set<MergeDataGroup> set = map.computeIfAbsent(group.getSource(), k -> new LinkedHashSet<>());
			set.add(group);
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

	protected static WhenDataGroup findWhenDataGroup(final DataGroup group) {
		for (DataEntry entry : group.getEntries()) {
			if (entry instanceof WhenDataGroup)
				return (WhenDataGroup) entry;
		}
		return null;
	}

	@FunctionalInterface
	protected interface MatchGetter {

		public Map<DataGroup, Set<MergeDataGroup>> get(final GoalRequirementArchiveSector sector);

	}
}
