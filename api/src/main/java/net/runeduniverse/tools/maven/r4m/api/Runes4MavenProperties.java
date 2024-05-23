/*
 * Copyright © 2024 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.api;

import net.runeduniverse.lib.utils.maven.MavenProperties;
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
