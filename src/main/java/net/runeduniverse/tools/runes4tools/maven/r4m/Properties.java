package net.runeduniverse.tools.runes4tools.maven.r4m;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.MavenProperties;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.Runes4MavenProperties;

public interface Properties extends MavenProperties {
	public static String VAL_PREFIX = "${";
	public static String VAL_POSTFIX = "}";

	public static String PLUGIN_KEY = Runes4MavenProperties.GROUP_ID + ':' + Runes4MavenProperties.ARTIFACT_ID;
	public static String PREFIX_ID = Runes4MavenProperties.PREFIX_ID;

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

			public interface INVOKER extends Runes4MavenProperties.LIFECYCLE.BUILDER.INVOKER {
			}
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
