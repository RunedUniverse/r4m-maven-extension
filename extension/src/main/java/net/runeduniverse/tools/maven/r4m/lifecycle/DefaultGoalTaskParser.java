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
package net.runeduniverse.tools.maven.r4m.lifecycle;

import org.apache.maven.lifecycle.internal.GoalTask;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.lifecycle.api.GoalTaskData;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.TaskParser;

@Component(role = TaskParser.class, hint = DefaultGoalTaskParser.HINT)
public class DefaultGoalTaskParser implements TaskParser {

	public static final String HINT = "goal";

	@Override
	public GoalTaskData parse(Object task) {
		if (!(task instanceof GoalTask))
			return null;
		final String goalTask = task.toString();
		String executionId = "default-cli";
		int executionIdx = goalTask.indexOf('@');
		if (executionIdx > 0) {
			executionId = goalTask.substring(executionIdx + 1);
		}

		return new Data(new String[] { "default" }, executionId.split(","), goalTask);
	}

	private class Data implements GoalTaskData {

		private final String[] modes;
		private final String[] executions;
		private final String goalTask;

		public Data(final String[] modes, final String[] executions, final String goalTask) {
			this.modes = modes;
			this.executions = executions;
			this.goalTask = goalTask;
		}

		@Override
		public String[] getModes() {
			return this.modes;
		}

		@Override
		public String[] getExecutions() {
			return this.executions;
		}

		@Override
		public String getGoalTask() {
			return this.goalTask;
		}

	}

}
