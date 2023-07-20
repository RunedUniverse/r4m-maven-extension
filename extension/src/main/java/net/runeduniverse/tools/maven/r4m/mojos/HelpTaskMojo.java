/*
 * Copyright © 2023 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.mojos;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * prints the task help-page
 *
 * @goal help-task
 * @requiresProject false
 * @since 1.0.0
 * @author VenaNocta
 */
public class HelpTaskMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("");
		getLog().info("\033[1mRunes4Maven Goal/Lifecycle Task Help\033[m");
		getLog().info("");
		getLog().info(" Maven tasks can be either goal or lifecycle tasks.");
		getLog().info("   Lifecycle Tasks are sometimes refered to as 'phases'.");
		getLog().info("");
		getLog().info(" Goal Tasks:");
		getLog().info("   <plugin-prefix>:<goal>[@<execution>]");
		getLog().info("   <plugin-group-id>:<plugin-artifact-id>[:<plugin-version>]:<goal>[@<execution>]");
		getLog().info("");
		getLog().info(" Lifecycle Tasks:");
		getLog().info("   [<modes>/]<lifecycle-task>[@<executions>]");
		getLog().info("");
		getLog().info(" <modes>");
		getLog().info("   Modes are a comma separated list of active modes.");
		getLog().info("   By default the modes 'default' and 'dev' are available");
		getLog().info("   but you may define others within the pem.xml config.");
		getLog().info("");
		getLog().info(" <lifecycle-task>");
		getLog().info("  Lifecycle Tasks are a comma separated list of active lifecycle-phases.");
		getLog().info("  Every Lifecycle Phase may have additional modifiers before and after it:");
		getLog().info("      ']' '[' <phase> ']' '['");
		getLog().info("");
		getLog().info(" Modifiers:");
		getLog().info("   ']' before the phase:");
		getLog().info("       select all phases before the phase excluding the phase itself");
		getLog().info("   '[' before the phase:");
		getLog().info("       select all phases after the phase including the phase itself");
		getLog().info("   ']' after the phase:");
		getLog().info("       select all phases before the phase including the phase itself");
		getLog().info("   '[' after the phase:");
		getLog().info("       select all phases after the phase excluding the phase itself");
		getLog().info("");
		getLog().info(" <executions>");
		getLog().info("   Executions are a comma separated list of active executions.");
		getLog().info("");
		getLog().info(" Properties:");
		getLog().info("     'r4m.lifecycle-task-request-calculator(-on-fork)'");
		getLog().info("   option 'declared'   -> '<phase>' => '[<phase>]'");
		getLog().info("       Effectively only running the declared phase itself.");
		getLog().info("   option 'sequential' -> '<phase>' =>  '<phase>]'");
		getLog().info("       Effectively running all phases inside the lifecycle of the phase");
		getLog().info("       including the phase itself. (This is the Maven Default)");
		getLog().info("");
		getLog().info("");
		getLog().info(" Examples:");
		getLog().info("   Install everything without running tests in dev mode:");
		getLog().info("       dev/]test[,install]");
		getLog().info("");
		getLog().info("   Run all the tests without recompiling with the pipeline execution active:");
		getLog().info("       [test]@pipeline");
		getLog().info("");
		getLog().info("   Package all with custom defined mode 'my-mode' and executions pipeline & test:");
		getLog().info("       my-mode/package]@pipeline,test");
		getLog().info("");
		getLog().info("");
	}

}
