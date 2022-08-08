package net.runeduniverse.tools.runes4tools.maven.runes4maven;

public interface Runes4MavenProperties {
	public interface LIFECYCLE {
		public interface BUILDER {
			public static String SOURCE_DIR = "runes4maven.builder.main.source.dir";
			public static String TEST_SOURCE_DIR = "runes4maven.builder.test.source.dir";
			public static String TARGET_DIR = "runes4maven.builder.target.dir";
		}
	}
}
