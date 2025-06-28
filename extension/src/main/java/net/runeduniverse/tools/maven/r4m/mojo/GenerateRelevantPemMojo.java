/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfigFactory;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionFilterUtils;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionRestrictionEvaluator;
import net.runeduniverse.tools.maven.r4m.pem.api.ModelPredicate;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelOverrideFilterSupplier;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSector;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSectorSnapshot;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelWriter;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.acquireExecutionArchive;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.mojoFailureExtensionLoading;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.reduce;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.replaceWithEquivalents;
import static net.runeduniverse.tools.maven.r4m.pem.api.ExecutionFilterUtils.defaultRelevanceFilterSupplier;

/**
 * generates the rel-pem.xml from all relevant executions
 *
 * @goal gen-rel-pem
 * @requiresProject true
 * @requiresReports false
 * @threadSafe true
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
	 * @component roleHint="xml"
	 */
	private ProjectExecutionModelWriter writer;
	/**
	 * @component
	 */
	private ExecutionArchiveSelectorConfigFactory cnfFactory;
	/**
	 * @component
	 */
	private ExecutionRestrictionEvaluator restrictionEvaluator;

	/**
	 * @component
	 */
	private Set<ProjectExecutionModelOverrideFilterSupplier> overrideFilterSupplier;

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

		final Set<Execution> executions = new LinkedHashSet<>();
		final int sectorCnt = collectExecutions(executions, projectSector.snapshot(), cnf);
		// clone! originals must not be modified!!!
		replaceWithEquivalents(executions);

		getLog().info("");
		getLog().info("Discovered & Relevant");
		getLog().info(String.format("    project depth:      %s", sectorCnt));
		getLog().info(String.format("    executions:         %s", executions.size()));
		getLog().info("    ------------------------");

		reduce(executions);

		getLog().info(String.format("    reduced executions: %s", executions.size()));

		final ProjectExecutionModel model = new ProjectExecutionModel();
		model.setVersion(Runes4MavenProperties.PROJECT_EXECUTION_MODEL_VERSION);
		model.setEffective(true);
		model.addExecutions(executions);

		final File xmlFile = new File(this.buildDir, "rel-pem.xml");
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

	private int collectExecutions(final Set<Execution> executions, final ExecutionArchiveSectorSnapshot snapshot,
			final ExecutionArchiveSelectorConfig cnf) {
		final Data data = new Data();
		final Map<String, AtomicBoolean> overrides = snapshot.collectOverridesAsBooleanMap();
		collectExecutions(executions, snapshot, overrides, defaultRelevanceFilterSupplier(restrictionEvaluator, cnf),
				false, data);
		return data.getDepth();
	}

	private void collectExecutions(final Set<Execution> executions, final ExecutionArchiveSectorSnapshot snapshot,
			final Map<String, AtomicBoolean> overrides, final ModelPredicate<ProjectExecutionModel, Execution> filter,
			final boolean requireInherited, final Data data) {
		if (snapshot == null)
			return;
		snapshot.applyOverrides(overrides, this.overrideFilterSupplier);

		data.incrementDepth();
		Set<Execution> applicableExecutions = snapshot.getEffectiveExecutions(filter, requireInherited);

		if (applicableExecutions.isEmpty()) {
			if (snapshot.getParent() != null)
				collectExecutions(executions, snapshot.getParent(), overrides, filter, true, data);

			if (!data.isEffExecDetected())
				applicableExecutions = snapshot.getExecutions(filter, requireInherited);
		} else
			data.setEffExecDetected(true);

		executions.addAll(applicableExecutions);
		executions.addAll(snapshot.getUserDefinedExecutions(filter, requireInherited));
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
