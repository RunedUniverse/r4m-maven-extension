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
package net.runeduniverse.tools.maven.r4m.grm.check;

import net.runeduniverse.lib.utils.conditional.ACondition;
import net.runeduniverse.lib.utils.conditional.api.Condition;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;
import net.runeduniverse.tools.maven.r4m.grm.view.api.GoalView;
import net.runeduniverse.tools.maven.r4m.grm.view.api.ProjectView;
import net.runeduniverse.tools.maven.r4m.grm.view.api.RuntimeView;

public abstract class ACheck extends ACondition<EntityView> implements Condition<EntityView> {

	private final String type;

	public ACheck(final String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return this.type;
	}

	protected static DataCheck<EntityView> project(final DataCheck<ProjectView> check) {
		return d -> check.eval(d.getProject());
	}

	protected static DataCheck<EntityView> runtime(final DataCheck<RuntimeView> check) {
		return d -> check.eval(d.getRuntime());
	}

	protected static DataCheck<EntityView> goal(final DataCheck<GoalView> check) {
		return d -> check.eval(d.getGoal());
	}
}
