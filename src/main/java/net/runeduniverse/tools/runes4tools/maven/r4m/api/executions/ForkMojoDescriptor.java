package net.runeduniverse.tools.runes4tools.maven.r4m.api.executions;

import org.apache.maven.model.Plugin;

public interface ForkMojoDescriptor {
	Plugin getMvnPlugin();

	String getPhase();

	String getGoal();

	String getExecutionId();

	public static ForkMojoDescriptor create(Plugin mvnPlugin, String phase, String goal, String executionId) {
		return new ForkMojoDescriptor() {
			@Override
			public Plugin getMvnPlugin() {
				return mvnPlugin;
			}

			@Override
			public String getPhase() {
				return phase;
			}

			@Override
			public String getGoal() {
				return goal;
			}

			@Override
			public String getExecutionId() {
				return executionId;
			}
		};
	}
}
