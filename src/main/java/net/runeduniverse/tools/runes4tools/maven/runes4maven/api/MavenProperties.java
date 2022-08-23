package net.runeduniverse.tools.runes4tools.maven.runes4maven.api;

public interface MavenProperties {
	public static String VAL_PREFIX = "${";
	public static String VAL_POSTFIX = "}";
	public static String VAL_DEFAULT_LIFECYCLE_ID = "default";
	public static String VAL_DEFAULT_EXECUTION_ID = "default";

	public interface PROJECT {
		public interface BUILD {
			public static String PARAM_SOURCE_DIR = "project.build.sourceDirectory";
			public static String PARAM_OUTPUT_DIR = "project.build.outputDirectory";

			public static String PARAM_TEST_SOURCE_DIR = "project.build.testSourceDirectory";
			public static String PARAM_TEST_OUTPUT_DIR = "project.build.testOutputDirectory";

			public static String PARAM_SCRIPT_SOURCE_DIR = "project.build.scriptSourceDirectory";
		}
	}
}
