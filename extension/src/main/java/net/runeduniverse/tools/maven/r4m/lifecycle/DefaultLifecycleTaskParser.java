package net.runeduniverse.tools.maven.r4m.lifecycle;

import org.apache.maven.lifecycle.internal.LifecycleTask;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskData;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskParser;

@Component(role = LifecycleTaskParser.class)
public class DefaultLifecycleTaskParser implements LifecycleTaskParser {

	/**
	 * Split task of format {@code [<mode>/][+]<phase>[@<execution>]}
	 */
	@Override
	public LifecycleTaskData parse(final LifecycleTask lifecycleTask) {
		String task = lifecycleTask.getLifecyclePhase();
		String mode = null;

		int splitIdx = task.indexOf('/');
		if (0 <= splitIdx) {
			mode = task.substring(0, splitIdx);
			task = task.substring(splitIdx + 1);
		}

		String execution = null;
		splitIdx = task.indexOf('@');
		if (0 < splitIdx) {
			execution = task.substring(splitIdx + 1);
			task = task.substring(0, splitIdx);
		}

		return new Data(mode, task, execution);
	}

	private class Data implements LifecycleTaskData {

		private final String mode;
		private final String phase;
		private final String execution;

		public Data(final String mode, final String phase, final String execution) {
			this.mode = mode;
			this.phase = phase;
			this.execution = execution;
		}

		@Override
		public String getMode() {
			return this.mode;
		}

		@Override
		public String getLifecyclePhase() {
			return this.phase;
		}

		@Override
		public String getExecution() {
			return this.execution;
		}

	}

}
