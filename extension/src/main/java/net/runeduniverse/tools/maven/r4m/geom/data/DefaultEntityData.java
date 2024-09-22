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
package net.runeduniverse.tools.maven.r4m.geom.data;

import net.runeduniverse.tools.maven.r4m.geom.model.data.EntityData;
import net.runeduniverse.tools.maven.r4m.geom.model.data.GoalData;
import net.runeduniverse.tools.maven.r4m.geom.model.data.ProjectData;
import net.runeduniverse.tools.maven.r4m.geom.model.data.RuntimeData;

public class DefaultEntityData implements EntityData {

	protected ProjectData project = null;
	protected RuntimeData runtime = null;
	protected GoalData goal = null;

	protected DefaultEntityData() {
	}

	public DefaultEntityData(final ProjectData projectData, final RuntimeData runtimeData, final GoalData goalData) {
		this.project = projectData;
		this.runtime = runtimeData;
		this.goal = goalData;
	}

	@Override
	public ProjectData getProject() {
		return this.project;
	}

	@Override
	public RuntimeData getRuntime() {
		return this.runtime;
	}

	@Override
	public GoalData getGoal() {
		return this.goal;
	}
}
