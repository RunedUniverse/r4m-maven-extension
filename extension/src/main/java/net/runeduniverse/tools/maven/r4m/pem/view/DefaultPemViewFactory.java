/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.pem.view;

import net.runeduniverse.tools.maven.r4m.pem.view.api.ExecutionView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.GoalView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.LifecycleView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.PhaseView;

public class DefaultPemViewFactory {

	public static ExecutionView createExecution(final String id) {
		return new DefaultExecutionView(id);
	}

	public static LifecycleView createLifecycle(final String id) {
		return new DefaultLifecycleView(id);
	}

	public static PhaseView createPhase(final String id) {
		return new DefaultPhaseView(id);
	}

	public static GoalView createGoal(final String groupId, final String artifactId, final String goalId) {
		return new DefaultGoalView(groupId, artifactId, goalId);
	}

}
