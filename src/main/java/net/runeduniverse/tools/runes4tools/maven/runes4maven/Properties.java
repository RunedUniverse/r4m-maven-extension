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

	public interface METAINF {
		public static String PATH = "META-INF/";

		public interface MAVEN {
			public static String PATH = METAINF.PATH + "maven/";
			public static String PLUGIN_DESCRIPTOR = PATH + "plugin.xml";
			public static String EXTENSIONS = PATH + "extension.xml";
		}

		public interface RUNES4MAVEN {
			public static String PATH = METAINF.PATH + "runes4maven/";
			public static String EXECUTIONS = PATH + "executions.xml";
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
