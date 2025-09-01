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
package net.runeduniverse.tools.maven.r4m.grm.api;

import java.util.Collection;
import java.util.Comparator;

import net.runeduniverse.lib.utils.logging.log.api.Recordable;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;

public interface GoalRequirementArchiveSelection extends Recordable, Cloneable {

	public Comparator<EntityView> getComparator();

	public GoalRequirementArchiveSelectorConfig getSelectorConfig();

	public GoalRequirementArchiveSelection clone();

	public GoalRequirementArchiveSelection filterByApplicableData(Collection<EntityView> entities);

}
