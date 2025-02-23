/*
 * Copyright © 2024 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.mojo;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.acquireExecutionArchive;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.mojoFailureExtensionLoading;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.reduce;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.replaceWithEquivalents;
import static net.runeduniverse.tools.maven.r4m.pem.api.ExecutionFilterUtils.defaultRelevanceFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfigFactory;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSector;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionFilter;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelWriter;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

/**
 * generates the rel-pem.xml from all relevant executions
 *
 * @goal gen-rel-pem
 * @since 1.0.0
 * @author VenaNocta
 */
public class GenerateRelevantPemMojo extends AbstractMojo {

	public static final String ERR_MSG_FAILED_TO_WRITE_TO_FILE = "Failed to write to file: %s";

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
	private File buildDir;
	/**
	 * @parameter default-value="${project.build.sourceEncoding}"
	 * @readonly
	 */
	private String encoding;
	/**
	 * @component
	 */
	private ExecutionArchive archive;
	/**
	 * @component
	 */
	private ProjectExecutionModelWriter writer;
	/**
	 * @component
	 */
	private ExecutionArchiveSelectorConfigFactory cnfFactory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (isBlank(this.encoding))
			this.encoding = "UTF-8";

		ExecutionArchiveSector projectSector = null;
		if (this.archive != null)
			projectSector = this.archive.getSector(this.mvnProject);

		if (projectSector == null) {
			// try loading via build-extension classrealm
			this.archive = acquireExecutionArchive(mvnSession, (ClassRealm) Thread.currentThread()
					.getContextClassLoader());
		}
		if (this.archive != null)
			projectSector = this.archive.getSector(this.mvnProject);

		if (projectSector == null)
			mojoFailureExtensionLoading(getLog());

		final ExecutionArchiveSelectorConfig cnf = this.cnfFactory.createEmptyConfig();
		cnf.selectActiveProject(this.mvnProject);
		cnf.selectPackagingProcedure(this.mvnProject.getPackaging());
		cnf.compile(this.mvnSession);

		Set<Execution> executions = new LinkedHashSet<>();
		int sectorCnt = collectExecutions(executions, projectSector, cnf);
		// clone! originals must not be modified!!!
		replaceWithEquivalents(executions);

		getLog().info("");
		getLog().info("Discovered & Relevant");
		getLog().info(String.format("    project depth:      %s", sectorCnt));
		getLog().info(String.format("    executions:         %s", executions.size()));
		getLog().info("    ------------------------");

		reduce(executions);

		getLog().info(String.format("    reduced executions: %s", executions.size()));

		ProjectExecutionModel model = new ProjectExecutionModel();
		model.setVersion(Runes4MavenProperties.PROJECT_EXECUTION_MODEL_VERSION);
		model.addExecutions(executions);

		File xmlFile = new File(this.buildDir, "rel-pem.xml");
		buildDir.mkdirs();

		try (OutputStream stream = new FileOutputStream(xmlFile, false)) {
			this.writer.writeModel(stream, model, this.encoding);
		} catch (IOException e) {
			throw new MojoFailureException(String.format(ERR_MSG_FAILED_TO_WRITE_TO_FILE, xmlFile.getPath()), e);
		}

		getLog().info("");
		getLog().info("Wrote relevant Project Execution Model (PEM) to:");
		getLog().info(String.format("    %s", Paths.get(this.mvnSession.getExecutionRootDirectory())
				.relativize(xmlFile.toPath())
				.toString()));
		getLog().info("");
	}

	private int collectExecutions(final Set<Execution> executions, final ExecutionArchiveSector sector,
			final ExecutionArchiveSelectorConfig cnf) {
		Data data = new Data();
		collectExecutions(executions, sector, defaultRelevanceFilter(cnf), false, data);
		return data.getDepth();
	}

	private void collectExecutions(final Set<Execution> executions, final ExecutionArchiveSector sector,
			final ExecutionFilter filter, final boolean onlyInherited, final Data data) {
		if (sector == null)
			return;

		data.incrementDepth();
		Set<Execution> applicableExecutions = sector.getEffectiveExecutions(filter, onlyInherited);

		if (applicableExecutions.isEmpty()) {
			if (sector.getParent() != null)
				collectExecutions(executions, sector.getParent(), filter, true, data);

			if (!data.isEffExecDetected())
				applicableExecutions = sector.getExecutions(filter, onlyInherited);
		} else
			data.setEffExecDetected(true);

		executions.addAll(applicableExecutions);
	}

	private static class Data {

		private int depth = 0;
		private boolean effExecDetected = false;

		public int getDepth() {
			return depth;
		}

		public boolean isEffExecDetected() {
			return effExecDetected;
		}

		public void incrementDepth() {
			this.depth = this.depth + 1;
		}

		public void setEffExecDetected(boolean effExecDetected) {
			this.effExecDetected = effExecDetected;
		}

	}

}
