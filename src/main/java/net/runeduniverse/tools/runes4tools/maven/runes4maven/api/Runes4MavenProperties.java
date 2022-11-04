package net.runeduniverse.tools.runes4tools.maven.runes4maven.api;

public interface Runes4MavenProperties {
	public interface LIFECYCLE {
		public interface INJECT {
			public static String LIFECYCLE_HINT = "r4m-inject";

			public static String PHASE_BUILDER = LIFECYCLE_HINT + "#builder";
			public static String PHASE_BUILDER_default = PHASE_BUILDER + "@default";
			public static String PHASE_BUILDER_test = PHASE_BUILDER + "@test";
		}

		public interface BUILDER {
			public static String PARAM_SOURCE_DIR = "runes4maven.builder.main.source.dir";
			public static String PARAM_TEST_SOURCE_DIR = "runes4maven.builder.test.source.dir";
			public static String PARAM_TARGET_DIR = "runes4maven.builder.target.dir";
		}
	}

	public interface METAINF extends MavenProperties.METAINF {
		public interface RUNES4MAVEN {
			public static String PATH = METAINF.PATH + "runes4maven/";
			public static String EXECUTIONS = PATH + "executions.xml";
		}
	}
}
