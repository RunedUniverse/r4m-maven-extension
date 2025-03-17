/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import net.runeduniverse.tools.maven.r4m.R4MProperties;
import net.runeduniverse.tools.maven.r4m.api.Settings;

import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.supportsExtensionFeatures;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.warnExtensionFeatureState;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * prints the help-page
 *
 * @goal help
 * @requiresProject false
 * @requiresReports false
 * @threadSafe true
 * @since 1.0.0
 * @author VenaNocta
 */
public class HelpMojo extends AbstractMojo {

	public static final String POM_PROP_FILE_TEMPLATE = "META-INF/maven/%s/%s/pom.properties";

	/**
	 * @component
	 */
	private Settings settings;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final Properties p = new Properties();
		try (final InputStream stream = getClass().getClassLoader()
				.getResourceAsStream(
						String.format(POM_PROP_FILE_TEMPLATE, R4MProperties.GROUP_ID, R4MProperties.ARTIFACT_ID))) {
			if (stream != null) {
				p.load(stream);
			}
		} catch (IOException | IllegalArgumentException ignored) {
			if (getLog().isDebugEnabled())
				getLog().error("Failed to load pom.properties from Mojo source!", ignored);
		}
		String version = p.getProperty("version");
		version = version == null ? "" : " (v" + version.trim() + ")";

		boolean supported = supportsExtensionFeatures(this.settings);
		if (!supported)
			warnExtensionFeatureState(getLog());
		getLog().info("");
		getLog().info("\033[1mRunes4Maven Help" + version + "\033[m");
		getLog().info("");
		getLog().info(" r4m:help");
		getLog().info("     Prints this help-page");
		getLog().info("");
		getLog().info(" r4m:help-tasks");
		getLog().info("     Prints goal/lifecycle tasks help-page.");
		getLog().info("     It describes how the new build argument 'lifecycle-tasks' works.");
		getLog().info("");
		if (supported) {
			extInfo();
		}
		getLog().info(" r4m:status");
		getLog().info("     Shows the status of all r4m features.");
		getLog().info("");
		getLog().info(" r4m:setup");
		getLog().info("     Configures the r4m-maven-extension" + version + " as a Core-Extension.");
		getLog().info("");
		getLog().info(" r4m:help-debug");
		getLog().info("     Prints the debug help-page.");
		getLog().info("     It lists debug goals which can help trace unexpected problems.");
		getLog().info("");
		if (!supported) {
			getLog().info("\033[1m ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ Disabled ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼\033[m");
			getLog().info("");
			extInfo();
		}
	}

	private void extInfo() {
		getLog().info(" r4m:gen-full-pem");
		getLog().info("     Discovers all loaded Executions which influence the current project");
		getLog().info("     build lifecycles. Discovered Executions will be condensed as much");
		getLog().info("     as possible and written to the 'full-pem.xml' file in the defined");
		getLog().info("     build directory.");
		getLog().info("");
		getLog().info(" r4m:gen-rel-pem");
		getLog().info("     Discovers all loaded Executions which are relevant to and influence");
		getLog().info("     the current project build lifecycles. Discovered Executions will be");
		getLog().info("     condensed as much as possible and written to the 'rel-pem.xml' file");
		getLog().info("     in the defined build directory.");
		getLog().info("");
	}
}
