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

import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.supportsExtensionFeatures;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.warnExtensionFeatureState;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import net.runeduniverse.tools.maven.r4m.api.Settings;

/**
 * prints the help-debug-page
 *
 * @goal help-debug
 * @requiresProject false
 * @requiresReports false
 * @threadSafe true
 * @since 1.1.0
 * @author VenaNocta
 */
public class HelpDebugMojo extends AbstractMojo {

	/**
	 * @component
	 */
	private Settings settings;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		boolean unsupported = !supportsExtensionFeatures(this.settings);
		if (unsupported)
			warnExtensionFeatureState(getLog());
		getLog().info("");
		getLog().info("\033[1mRunes4Maven Help: Debug\033[m");
		if (unsupported) {
			getLog().info("");
			getLog().info("\033[1m ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ Disabled ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼\033[m");
		}
		getLog().info("");
		getLog().info(" r4m:help-debug");
		getLog().info("     Prints this help-page");
		getLog().info("");
		getLog().info(" r4m:debug-show-pem-archive");
		getLog().info("     Prints all registered PEMs.");
		getLog().info("     The PEMs are logged as parsed, no selection has occurred!");
		getLog().info("     -------------------------------------------------------------------");
		getLog().info("     \033[1mHINT:\033[m Can be used to trace where an unwanted PEM originated from.");
		getLog().info("");
		getLog().info(" r4m:debug-show-grm-archive");
		getLog().info("     Prints all registered GRMs.");
		getLog().info("     The GRMs are logged as parsed, no selection has occurred!");
		getLog().info("     -------------------------------------------------------------------");
		getLog().info("     \033[1mHINT:\033[m Can be used to trace where an unwanted GRM originated from.");
		getLog().info("");
		getLog().info("\033[1m ----------------------------- Properties ------------------------------\033[m");
		getLog().info("");
		getLog().info(" r4m.debug.dump-grm-entries-before-execution");
		getLog().info("     Options: 'all' / 'reduced'");
		getLog().info("     -------------------------------------------------------------------");
		getLog().info("     This property defines the level of detail, when logging the");
		getLog().info("     grm-selection, pre task-execution in debug mode (-X)");
		getLog().info("");
	}
}
