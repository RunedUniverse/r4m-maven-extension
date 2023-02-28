package net.runeduniverse.tools.maven.r4m.mojos;

import java.io.File;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelWriter;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

import static net.runeduniverse.tools.maven.r4m.mojos.ExtensionUtils.acquireExecutionArchive;
import static net.runeduniverse.tools.maven.r4m.mojos.ExtensionUtils.mojoFailureExtensionLoading;
import static net.runeduniverse.tools.maven.r4m.mojos.ExtensionUtils.replaceWithEquivalents;
import static net.runeduniverse.tools.maven.r4m.mojos.ExtensionUtils.reduce;

/**
 * generates the full pem.xml from all active maven defaults
 * 
 * @author Pl4yingNight
 * @goal generate-full-pem
 */
public class GenerateFullPemMojo extends AbstractMojo {
	/**
	 * @parameter default-value="${session}"
	 * @readonly
	 */
	private MavenSession mvnSession;
	/**
	 * @parameter default-value="${project}"
	 * @readonly
	 */
	private MavenProject mvnProject;
	/**
	 * @parameter default-value="${project.build.directory}"
	 * @readonly
	 */
	private File builddir;
	/**
	 * @component
	 */
	private ExecutionArchive archive;
	/**
	 * @component
	 */
	private ProjectExecutionModelWriter writer;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub
		getLog().info("building full pem.xml");

		ExecutionArchiveSlice projectSlice = null;
		if (this.archive != null)
			projectSlice = this.archive.getSlice(this.mvnProject);

		if (projectSlice == null) {
			// try loading via build-extension classrealm
			this.archive = acquireExecutionArchive(mvnSession,
					(ClassRealm) Thread.currentThread().getContextClassLoader());
		}
		if (this.archive != null)
			projectSlice = this.archive.getSlice(this.mvnProject);

		if (projectSlice == null)
			mojoFailureExtensionLoading(getLog());

		Set<Execution> executions = new LinkedHashSet<>();
		int sliceCnt = collectExecutions(executions, projectSlice);
		// clone! originals must not be modified!!!
		replaceWithEquivalents(executions);

		getLog().info("");
		getLog().info("Discovery");
		getLog().info(String.format("    project depth:      %s", sliceCnt));
		getLog().info(String.format("    executions:         %s", executions.size()));
		getLog().info("    ------------------------");

		reduce(executions);

		getLog().info(String.format("    reduced executions: %s", executions.size()));
		getLog().info("");

		ProjectExecutionModel model = new ProjectExecutionModel();
		model.setVersion(Runes4MavenProperties.PROJECT_EXECUTION_MODEL_VERSION);
		model.addExecutions(executions);

		getLog().debug(model.toRecord().toString());

		PlexusConfiguration xml = this.writer.convert(model);
		
		getLog().warn(xml.toString());
	}

	private int collectExecutions(final Set<Execution> executions, final ExecutionArchiveSlice slice) {
		return collectExecutions(executions, slice, false);
	}

	private int collectExecutions(final Set<Execution> executions, final ExecutionArchiveSlice slice,
			final boolean onlyInherited) {
		if (slice == null)
			return 0;
		executions.addAll(slice.getExecutions(e -> true, onlyInherited));
		return collectExecutions(executions, slice.getParent(), true) + 1;
	}
}
