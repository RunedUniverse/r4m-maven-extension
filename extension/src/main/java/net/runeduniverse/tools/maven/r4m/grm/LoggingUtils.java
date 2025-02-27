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

import net.runeduniverse.lib.utils.conditions.api.Condition;
import net.runeduniverse.lib.utils.conditions.tools.ConditionIndexer;
import net.runeduniverse.lib.utils.conditions.tools.Entry;
import net.runeduniverse.lib.utils.conditions.tools.EntrySet;
import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;

public class LoggingUtils {

	public static CompoundTree toRecord(final EntrySet<?> set) {
		if (set == null)
			return null;
		if (set instanceof Recordable)
			return ((Recordable) set).toRecord();

		final CompoundTree tree = new CompoundTree(set.getClass()
				.getCanonicalName());
		for (Entry<?> entry : set) {
			final CompoundTree subTree = toRecord(entry);
			if (subTree == null)
				continue;
			tree.append(subTree);
		}
		return tree;
	}

	public static CompoundTree toRecord(final Entry<?> entry) {
		if (entry == null)
			return null;
		if (entry instanceof Recordable)
			return ((Recordable) entry).toRecord();

		final CompoundTree tree = new CompoundTree(entry.getClass()
				.getCanonicalName());
		final ConditionIndexer indexer = new ConditionIndexer();
		tree.append("valid", Boolean.toString(entry.validate(indexer)));

		CompoundTree subTree = new CompoundTree("match entity");
		Condition<?> con = entry.getMatchItem();
		if (con != null) {
			subTree.append(indexer.toRecord(con));
			tree.append(subTree);
		}
		subTree = new CompoundTree("execute after");
		con = entry.getMatchBefore();
		if (con != null) {
			subTree.append(indexer.toRecord(con));
			tree.append(subTree);
		}
		subTree = new CompoundTree("execute before");
		con = entry.getMatchAfter();
		if (con != null) {
			subTree.append(indexer.toRecord(con));
			tree.append(subTree);
		}

		return tree;
	}
}
