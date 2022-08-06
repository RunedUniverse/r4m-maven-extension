package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.builder;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;



/**
 * Forks the builder lifecycle
 * @author basti
 * @goal builder
 * @execute lifecycle="builder" phase="builder-package"
 */
public class ExecuteMojo extends ABuilderMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Sucessfully built Sources!");
	}

}
