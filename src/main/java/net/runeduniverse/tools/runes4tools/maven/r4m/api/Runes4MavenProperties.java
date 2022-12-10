package net.runeduniverse.tools.runes4tools.maven.r4m.api;

public interface Runes4MavenProperties {
	public static String GROUP_ID = "net.runeduniverse.tools.runes4tools";
	public static String ARTIFACT_ID = "runes4maven-maven-plugin";
	public static String PREFIX_ID = "r4m";

	public interface LIFECYCLE {
		public interface INJECT {
			public static String LIFECYCLE_HINT = PREFIX_ID + "-inject";

			public static String PHASE_BUILDER = LIFECYCLE_HINT + "#builder";
			public static String PHASE_BUILDER_default = PHASE_BUILDER + "@default";
			public static String PHASE_BUILDER_test = PHASE_BUILDER + "@test";
		}

		public interface BUILDER {
			public static String LIFECYCLE_HINT = "builder";

			public static String EXECUTION_DEFAULT_ID = "default";
			public static String EXECUTION_TEST_ID = "test";

			public static String PARAM_SOURCE_DIR = "runes4maven.builder.main.source.dir";
			public static String PARAM_TEST_SOURCE_DIR = "runes4maven.builder.test.source.dir";
			public static String PARAM_TARGET_DIR = "runes4maven.builder.target.dir";

			public interface INVOKER {
				public static String LIFECYCLE_INVOKER_HINT = "build-invoker";

				public static String PHASE_BUILD = "build";
				public static String PHASE_BUILD_TEST = PHASE_BUILD + "-test";

				public static String MOJO_SUMMARY = "build-invoker-summary";
			}
		}
	}

	public interface METAINF extends MavenProperties.METAINF {
		public interface RUNES4MAVEN {
			public static String PATH = METAINF.PATH + "runes4maven/";
			public static String EXECUTIONS = PATH + "executions.xml";
		}
	}
}
