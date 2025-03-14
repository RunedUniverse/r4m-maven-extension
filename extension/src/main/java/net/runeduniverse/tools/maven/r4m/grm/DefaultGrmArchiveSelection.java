/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
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
import java.util.Comparator;

import net.runeduniverse.lib.utils.conditional.tool.ConditionComparator;
import net.runeduniverse.lib.utils.conditional.tool.RelationEntrySet;
import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSelection;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;

public class DefaultGrmArchiveSelection implements GoalRequirementArchiveSelection {

	protected final GoalRequirementArchiveSelectorConfig selectorConfig;
	protected final RelationEntrySet<EntityView> conditionSet;

	public DefaultGrmArchiveSelection(final GoalRequirementArchiveSelectorConfig selectorConfig,
			final RelationEntrySet<EntityView> conditionSet) {
		this.selectorConfig = selectorConfig;
		this.conditionSet = conditionSet;
	}

	@Override
	public Comparator<EntityView> getComparator() {
		return new ConditionComparator<EntityView>(this.conditionSet);
	}

	@Override
	public GoalRequirementArchiveSelectorConfig getSelectorConfig() {
		return this.selectorConfig;
	}

	@Override
	public GoalRequirementArchiveSelection clone() {
		return new DefaultGrmArchiveSelection(getSelectorConfig().clone(), this.conditionSet.clone());
	}

	@Override
	public GoalRequirementArchiveSelection filterByApplicableData(final Collection<EntityView> entities) {
		this.conditionSet.filterByApplicableData(entities);
		return this;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("GRM Selection");

		tree.append(LoggingUtils.toRecord(this.conditionSet));

		return tree;
	}

	@Override
	public String toString() {
		return toRecord().toString();
	}
}
