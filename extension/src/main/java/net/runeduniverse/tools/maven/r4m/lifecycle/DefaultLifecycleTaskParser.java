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

import org.apache.maven.lifecycle.internal.LifecycleTask;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskData;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.TaskParser;

@Component(role = TaskParser.class, hint = DefaultLifecycleTaskParser.HINT)
public class DefaultLifecycleTaskParser implements TaskParser {

	public static final String HINT = "lifecycle";

	/**
	 * Split task of format {@code [<modes>/]<lifecycle-task/phase>[@<executions>]}
	 */
	@Override
	public LifecycleTaskData parse(final Object taskObject) {
		if (!(taskObject instanceof LifecycleTask))
			return null;
		final LifecycleTask lifecycleTask = (LifecycleTask) taskObject;
		String task = lifecycleTask.getLifecyclePhase();
		String mode = "";

		int splitIdx = task.indexOf('/');
		if (0 <= splitIdx) {
			mode = task.substring(0, splitIdx);
			task = task.substring(splitIdx + 1);
		}

		String execution = "";
		splitIdx = task.indexOf('@');
		if (0 < splitIdx) {
			execution = task.substring(splitIdx + 1);
			task = task.substring(0, splitIdx);
		}

		return new Data(mode.split(","), execution.split(","), task);
	}

	private class Data implements LifecycleTaskData {

		private final String[] modes;
		private final String[] executions;
		private final String task;

		public Data(final String[] modes, final String[] executions, final String task) {
			this.modes = modes;
			this.executions = executions;
			this.task = task;
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
		public String getLifecycleTask() {
			return this.task;
		}

	}

}
