package net.runeduniverse.tools.runes4tools.maven.runes4maven;

import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.MavenProperties;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.Runes4MavenProperties;

public interface Properties {
	public static String VAL_PREFIX = MavenProperties.VAL_PREFIX;
	public static String VAL_POSTFIX = MavenProperties.VAL_POSTFIX;
	public static String DEFAULT_LIFECYCLE_ID = MavenProperties.VAL_DEFAULT_LIFECYCLE_ID;
	public static String DEFAULT_EXECUTION_ID = MavenProperties.VAL_DEFAULT_EXECUTION_ID;

	public interface PROJECT {
		public interface BUILD extends MavenProperties.PROJECT.BUILD {
		}
	}

	public interface LIFECYCLE {
		public interface INJECT extends Runes4MavenProperties.LIFECYCLE.INJECT {
			public static String DEFAULT_EXECUTION_ID = Properties.DEFAULT_EXECUTION_ID;
		}

		public interface BUILDER extends Runes4MavenProperties.LIFECYCLE.BUILDER {
			public static String DEFAULT_VAL_SOURCE_DIR = VAL_PREFIX + Properties.PROJECT.BUILD.PARAM_SOURCE_DIR
					+ VAL_POSTFIX;
			public static String DEFAULT_VAL_TEST_SOURCE_DIR = VAL_PREFIX
					+ Properties.PROJECT.BUILD.PARAM_TEST_SOURCE_DIR + VAL_POSTFIX;
			public static String DEFAULT_VAL_TARGET_DIR = VAL_PREFIX + Properties.PROJECT.BUILD.PARAM_OUTPUT_DIR
					+ VAL_POSTFIX;
		}
	}

	public interface METAINF extends MavenProperties.METAINF {
		public interface MAVEN extends MavenProperties.METAINF.MAVEN {
		}

		public interface RUNES4MAVEN extends Runes4MavenProperties.METAINF.RUNES4MAVEN {
		}

		public interface PLEXUS extends MavenProperties.METAINF.PLEXUS {
		}

		public interface SISU extends MavenProperties.METAINF.SISU {
		}
	}
}
