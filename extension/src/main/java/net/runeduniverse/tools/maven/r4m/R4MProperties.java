package net.runeduniverse.tools.maven.r4m;

import net.runeduniverse.tools.maven.r4m.api.MavenProperties;
import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;

public interface R4MProperties extends MavenProperties, Runes4MavenProperties {
	public static String VAL_PREFIX = "${";
	public static String VAL_POSTFIX = "}";

	public static String PLUGIN_KEY = GROUP_ID + ':' + ARTIFACT_ID;

	public interface PROJECT {
		public interface BUILD extends MavenProperties.PROJECT.BUILD {
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
