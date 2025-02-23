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
package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.Recordable;
import net.runeduniverse.tools.maven.r4m.pem.view.api.ExecutionView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.GoalView;

public interface ExecutionArchiveSelection extends Recordable {

	public void modify(Modification mod);

	public Map<ExecutionView, List<GoalView>> selectPhase(String phase);

	public Map<String, Map<ExecutionView, List<GoalView>>> selectPhases(String... phases);

	public Map<String, Map<ExecutionView, List<GoalView>>> selectPhases(final Collection<String> phases);

	public ExecutionArchiveSelectorConfig getSelectorConfig();

	@FunctionalInterface
	public interface Modification {

		public void modify(final Set<ExecutionView> views);

	}
}
