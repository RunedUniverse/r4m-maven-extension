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
package net.runeduniverse.tools.maven.r4m.grm.api;

import net.runeduniverse.lib.utils.logging.logs.Recordable;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementModel;
import net.runeduniverse.tools.maven.r4m.indexer.api.ProjectBoundEntry;

public interface GoalRequirementArchiveSector extends ProjectBoundEntry<GoalRequirementArchiveSector>, Recordable {

	// public GoalRequirementModel getModel(Object sourcedModelEntry);

	public void register(GoalRequirementModel grm);

}
