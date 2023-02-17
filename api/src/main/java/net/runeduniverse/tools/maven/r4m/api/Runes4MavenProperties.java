package net.runeduniverse.tools.maven.r4m.api;

public interface Runes4MavenProperties {
	public static String GROUP_ID = "net.runeduniverse.tools.maven.r4m";
	public static String ARTIFACT_ID = "r4m-maven-extension";
	public static String PREFIX_ID = "r4m";

	public static String PROJECT_EXECUTION_MODEL_FILE = "pem.xml";

	public static String EXECUTIONS_PARSER_LIFECYCLE_PARTICIPANT_HINT = "r4m:executions-parser";

	public interface LIFECYCLE {
		public interface DEV {
			public static String LIFECYCLE_PARTICIPANT_HINT = "r4m:dev-lifecycle";
		}

		public interface BUILDER {
			public static String LIFECYCLE_HINT = "builder";

			public static String EXECUTION_DEFAULT_ID = "default";
			public static String EXECUTION_TEST_ID = "test";

			public static String PARAM_SOURCE_DIR = "r4m-builder.main.source.dir";
			public static String PARAM_TEST_SOURCE_DIR = "r4m-builder.test.source.dir";
			public static String PARAM_TARGET_DIR = "r4m-builder.target.dir";
		}
	}

	public interface METAINF extends MavenProperties.METAINF {
		public interface RUNES4MAVEN {
			public static String PATH = METAINF.PATH + "r4m/";
			public static String EXECUTIONS = PATH + "plugin-pem.xml";
		}
	}
}
