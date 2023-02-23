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
	}

	public interface METAINF extends MavenProperties.METAINF {
		public interface RUNES4MAVEN {
			public static String PATH = METAINF.PATH + "r4m/";
			public static String EXECUTIONS = PATH + "plugin-pem.xml";
		}
	}
}
