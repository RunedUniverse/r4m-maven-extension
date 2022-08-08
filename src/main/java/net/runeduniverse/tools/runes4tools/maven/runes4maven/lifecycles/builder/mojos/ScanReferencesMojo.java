package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.builder.mojos;

import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.builder.ReferenceFileScanner;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.builder.ReferenceScanner;

/**
 * Maps out all references of the source files to later be able to compile
 * source files in order
 * 
 * @author Pl4yingNight
 * @goal builder-scan-references
 * @phase builder-scan-references
 */
public class ScanReferencesMojo extends AMainBuilderMojo {

	@Requirement(role = ReferenceScanner.class)
	public ReferenceScanner scanner;
	@Requirement(role = ReferenceFileScanner.class)
	private Set<ReferenceFileScanner> fileScanner;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		/*
		 * getLog().info((this.runtime==null)+""); scanner.inject(this.runtime)
		 * .inject(this.fileScanner) .inject(this.references);
		 */

		getLog().info("mapping references of source-files");
		scanner.logInfo(getLog());
		// TODO collect collectors from compiler plugins and run those
		scanner.logAnalisis(getLog());
		getLog().info("finished mapping references of source-files");
	}
}
