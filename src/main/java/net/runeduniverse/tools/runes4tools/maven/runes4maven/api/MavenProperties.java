package net.runeduniverse.tools.runes4tools.maven.runes4maven.api;

public interface MavenProperties {
	public static String DEFAULT_LIFECYCLE_ID = "default";
	public static String DEFAULT_EXECUTION_ID = "default";

	public interface PROJECT {
		public interface BUILD {
			public static String PARAM_SOURCE_DIR = "project.build.sourceDirectory";
			public static String PARAM_OUTPUT_DIR = "project.build.outputDirectory";

			public static String PARAM_TEST_SOURCE_DIR = "project.build.testSourceDirectory";
			public static String PARAM_TEST_OUTPUT_DIR = "project.build.testOutputDirectory";

			public static String PARAM_SCRIPT_SOURCE_DIR = "project.build.scriptSourceDirectory";
		}
	}

	public interface METAINF {
		public static String PATH = "META-INF/";

		public interface MAVEN {
			public static String PATH = METAINF.PATH + "maven/";
			public static String PLUGIN_DESCRIPTOR = PATH + "plugin.xml";
			public static String EXTENSIONS = PATH + "extension.xml";
		}

		public interface PLEXUS {
			public static String PATH = METAINF.PATH + "plexus/";
			public static String COMPONENTS = PATH + "components.xml";
		}

		public interface SISU {
			public static String PATH = METAINF.PATH + "sisu/";
			public static String NAMED_CONFIG = PATH + "javax.inject.Named";
		}
	}
}
