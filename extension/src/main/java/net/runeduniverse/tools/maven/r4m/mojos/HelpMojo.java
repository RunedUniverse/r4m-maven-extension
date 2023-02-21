package net.runeduniverse.tools.maven.r4m.mojos;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;

/**
 * generates the full pem.xml from all active maven defaults
 * 
 * @author Pl4yingNight
 * @goal help
 */
public class HelpMojo extends AbstractMojo {

	/**
     * @component
     */
	private ExecutionArchive archive;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub
		getLog().info("Runes4Maven Help");

		if (this.archive == null) {
			getLog().warn("ExecutionArchive is null");
		} else {
			getLog().info("ExecutionArchive exists!");
		}

	}

}
