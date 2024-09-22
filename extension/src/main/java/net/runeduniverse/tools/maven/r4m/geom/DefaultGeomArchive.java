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
package net.runeduniverse.tools.maven.r4m.geom;

import java.util.Comparator;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.lib.utils.conditions.tools.ConditionComparator;
import net.runeduniverse.lib.utils.conditions.tools.ConditionIndexer;
import net.runeduniverse.lib.utils.conditions.tools.EntrySet;
import net.runeduniverse.tools.maven.r4m.geom.model.data.EntityData;

@Component(role = DefaultGeomArchive.class)
public class DefaultGeomArchive {

	public Comparator<EntityData> getComparator() {
		// TODO implement
		final EntrySet<EntityData> set = new EntrySet<>();
		set.compile(new ConditionIndexer());
		return new ConditionComparator<EntityData>(set);
	}
}
