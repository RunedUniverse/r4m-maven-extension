package net.runeduniverse.tools.runes4tools.maven.runes4maven.builder;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Maps out all references of the source files to later be able to compile
 * source files in order
 * 
 * @author basti
 * @goal builder-map-references
 */
public class MapReferencesMojo extends ABuilderMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("mapping references of source-files");
		// TODO collect collectors from compiler plugins and run those
		
		getLog().info("finished mapping references of source-files");
	}

}
