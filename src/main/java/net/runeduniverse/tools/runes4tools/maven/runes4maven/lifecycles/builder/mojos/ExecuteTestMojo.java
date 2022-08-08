package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.builder.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;



/**
 * Forks the builder lifecycle
 * 
 * @author Pl4yingNight
 * @goal builder-test
 * @execute lifecycle="builder-test" phase="builder-package"
 */
public class ExecuteTestMojo extends ABuilderMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Sucessfully built Test Sources!");
	}

}
