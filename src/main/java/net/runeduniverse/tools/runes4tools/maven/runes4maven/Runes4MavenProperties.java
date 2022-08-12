package net.runeduniverse.tools.runes4tools.maven.runes4maven;

public interface Runes4MavenProperties {
	public interface LIFECYCLE {
		public interface EXEC {
			public static String LIFECYCLE_HINT = "runes4maven-exec";

			public static String PHASE_BUILDER = "runes4maven-exec#builder";
			public static String PHASE_BUILDER_default = "runes4maven-exec#builder@default";
			public static String PHASE_BUILDER_test = "runes4maven-exec#builder@test";
		}

		public interface BUILDER {
			public static String PARAM_SOURCE_DIR = "runes4maven.builder.main.source.dir";
			public static String PARAM_TEST_SOURCE_DIR = "runes4maven.builder.test.source.dir";
			public static String PARAM_TARGET_DIR = "runes4maven.builder.target.dir";
		}
	}
}
