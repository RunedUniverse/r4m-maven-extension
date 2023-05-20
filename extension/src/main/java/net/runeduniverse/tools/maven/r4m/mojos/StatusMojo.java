package net.runeduniverse.tools.maven.r4m.mojos;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * prints the status-page
 * 
 * @author VenaNocta
 * @goal status
 * @requiresProject false
 */
public class StatusMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("");
		getLog().info("\033[1mRunes4Maven Status\033[m");
		getLog().info("");
		getLog().info(" r4m.");
		getLog().info("     Shows the status of all r4m features.");
		getLog().info("");
		getLog().info("");
	}

}
