package net.runeduniverse.tools.maven.r4m.mojos;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * prints the help-page
 * 
 * @author Pl4yingNight
 * @goal help
 * @requiresProject false
 */
public class HelpMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("");
		getLog().info("Runes4Maven Help");
		getLog().info("");
		getLog().info(" r4m:help");
		getLog().info("     prints this help-page");
		getLog().info("");
		getLog().info(" r4m:generate-full-pem");
		getLog().info("     Discovers all loaded Executions which influence the current project");
		getLog().info("     build lifecycles. Discovered Executions will be condensed as much");
		getLog().info("     as possible and written to the 'full-pem.xml' file in the defined");
		getLog().info("     build directory.");
		getLog().info("");
		getLog().info("");
	}

}
