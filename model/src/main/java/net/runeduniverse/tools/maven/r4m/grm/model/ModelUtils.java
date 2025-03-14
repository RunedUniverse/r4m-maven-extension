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
package net.runeduniverse.tools.maven.r4m.grm.model;

import java.util.Collection;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;

public final class ModelUtils {

	public static CompoundTree toRecord(DataEntry entry) {
		if (entry == null)
			return null;
		if (entry instanceof Recordable)
			return getRecord((Recordable) entry);
		if (entry instanceof DataGroup)
			return getRecord((DataGroup) entry);
		return getRecord(entry);
	}

	public static CompoundTree getRecord(DataEntry entry) {
		if (entry == null)
			return null;
		return createEntryTree(entry);
	}

	public static CompoundTree getRecord(DataGroup group) {
		if (group == null)
			return null;
		final CompoundTree tree = createEntryTree(group);
		appendEntries(tree, group.getEntries());
		return tree;
	}

	public static CompoundTree getRecord(Recordable recordable) {
		if (recordable == null)
			return null;
		return recordable.toRecord();
	}

	public static CompoundTree createEntryTree(final DataEntry entry) {
		final CompoundTree tree = new DefaultCompoundTree(entry instanceof DataGroup ? "group" : "entry", entry.type());
		tree.append("class", entry.getClass()
				.getCanonicalName());
		return tree;
	}

	public static void appendEntries(final CompoundTree tree, final Collection<DataEntry> entries) {
		if (entries == null)
			return;
		for (DataEntry data : entries) {
			final CompoundTree dataTree = toRecord(data);
			if (dataTree == null)
				continue;
			tree.append(dataTree);
		}
	}

	public static int hash(final Boolean b) {
		if (b == null)
			return 0;
		return b.hashCode();
	}

	public static int hash(final String s) {
		if (s == null)
			return 0;
		return s.hashCode();
	}

	public static int hash(final GoalRequirementSource s) {
		if (s == null)
			return 0;
		return s.key()
				.hashCode();
	}

	public static int hash(final GoalRequirementCombineMethod m) {
		if (m == null)
			return 0;
		return m.key()
				.hashCode();
	}

	public static boolean strEquals(final String a, final String b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		return a.equals(b);
	}
}
