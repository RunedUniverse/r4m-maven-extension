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
package net.runeduniverse.tools.maven.r4m.grm.model;

import java.util.Collection;

public interface DataGroup extends DataEntry {

	public Collection<DataEntry> getEntries();

	public boolean addEntry(DataEntry entry);

	public DataGroup copy();

	public default boolean valid() {
		final Collection<DataEntry> c = getEntries();
		if (c == null || c.isEmpty())
			return false;
		for (DataEntry e : c) {
			if (!e.valid())
				return false;
		}
		return true;
	}
}
