package net.runeduniverse.tools.maven.r4m.api;

import net.runeduniverse.tools.maven.r4m.pem.model.ModelProperties;

public interface Runes4MavenProperties {

	public static String GROUP_ID = "net.runeduniverse.tools.maven.r4m";
	public static String ARTIFACT_ID = "r4m-maven-extension";
	public static String PREFIX_ID = "r4m";

	public static String PROJECT_EXECUTION_MODEL_FILE = "pem.xml";
	public static String PROJECT_EXECUTION_MODEL_VERSION = ModelProperties.MODEL_VERSION;

	public static String R4M_LIFECYCLE_PARTICIPANT_HINT = "r4m:default";

	public static String BUILD_EXTENSION_REALM_ID = "extension>net.runeduniverse.tools.maven.r4m:r4m-maven-extension";

	public interface METAINF extends MavenProperties.METAINF {
		public interface RUNES4MAVEN {
			public static String PATH = METAINF.PATH + "r4m/";
			public static String EXECUTIONS = PATH + "plugin-pem.xml";
		}
	}

}
