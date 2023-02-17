package net.runeduniverse.tools.maven.r4m.lifecycles.builder.mojos;

import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.lifecycles.builder.ReferenceFileScanner;
import net.runeduniverse.tools.maven.r4m.lifecycles.builder.ReferenceScanner;

/**
 * Maps out all references of the source files to later be able to compile
 * source files in order
 * 
 * @author Pl4yingNight
 * @goal compiler-scan-test-references
 */
public class ScanTestReferencesMojo extends ATestBuilderMojo {

	@Requirement(role = ReferenceScanner.class)
	private ReferenceScanner scanner;
	@Requirement(role = ReferenceFileScanner.class)
	private Set<ReferenceFileScanner> fileScanner;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		/*
		 * scanner.inject(this.runtime) .inject(this.fileScanner)
		 * .inject(this.references);
		 */
		getLog().info("mapping references of test-source-files");
		// scanner.logInfo(getLog());
		// TODO collect collectors from compiler plugins and run those
		// scanner.logAnalisis(getLog());
		getLog().info("finished mapping references of test-source-files");
	}

}
