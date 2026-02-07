/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.tools.maven.r4m;

import net.runeduniverse.lib.utils.maven3.api.MavenProperties;
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
