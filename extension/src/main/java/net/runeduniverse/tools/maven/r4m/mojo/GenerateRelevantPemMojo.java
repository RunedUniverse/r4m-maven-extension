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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import net.runeduniverse.lib.utils.common.api.DataMap;
import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfigFactory;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionRestrictionEvaluator;
import net.runeduniverse.tools.maven.r4m.pem.api.ModelPredicate;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelOverrideFilterSupplier;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSector;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSectorSnapshot;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelWriter;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelSource;
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
	 * @component role="net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelOverrideFilterSupplier"
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
		cnf.selectTopLevelProject(this.mvnSession.getTopLevelProject());
		cnf.selectActiveProject(this.mvnProject);
		cnf.selectPackagingProcedure(this.mvnProject.getPackaging());
		cnf.compile(this.mvnSession);

		final Set<Execution> executions = new LinkedHashSet<>();
		final ExecutionArchiveSectorSnapshot snapshot = projectSector.snapshot();
		final DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> overrides = snapshot
				.collectOverridesAsBooleanMapWithModels();
		final int sectorCnt = collectExecutions(executions, snapshot, overrides.toValueMap(), cnf);
		// clone! originals must not be modified!!!
		replaceWithEquivalents(executions);

		if (!overrides.isEmpty())
			logOverrides(overrides, snapshot.getOverrideModelReference());

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
			final Map<String, AtomicBoolean> overrides, final ExecutionArchiveSelectorConfig cnf) {
		final Data data = new Data();
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
		data.setEffExecDetected(snapshot.hasModelWithEffectiveOverride(requireInherited));

		if (!data.isEffExecDetected() && snapshot.getParent() != null)
			collectExecutions(executions, snapshot.getParent(), overrides, filter, true, data);
		if (!data.isEffExecDetected())
			applicableExecutions = snapshot.getExecutions(filter, requireInherited);

		executions.addAll(applicableExecutions);
		executions.addAll(snapshot.getUserDefinedExecutions(filter, requireInherited));
	}

	private void logOverrides(final DataMap<String, AtomicBoolean, Set<ProjectExecutionModel>> overrides,
			final Map<String, String> refMap) {
		// check validity
		if (overrides == null || overrides.isEmpty())
			return;

		// print header
		getLog().info("");
		getLog().info("Active Overrides");

		// log active overrides
		int mxLen = 0;
		for (String id : overrides.keySet()) {
			id = refMap.getOrDefault(id, id);
			int len = id == null ? 4 : id.length();
			if (30 < len)
				continue;
			if (mxLen < len)
				mxLen = len;
		}

		// index all plugin artifacts in the project tree
		final Map<String, String> artifactIndex = new LinkedHashMap<>();
		for (MavenProject mvnPrj = mvnProject; mvnPrj != null; mvnPrj = mvnPrj.getParent()) {
			final String id = ModelSource.id(mvnPrj::getGroupId, mvnPrj::getArtifactId);
			for (Plugin plugin : mvnPrj.getBuildPlugins())
				artifactIndex.computeIfAbsent(ModelSource.id(plugin::getGroupId, plugin::getArtifactId), k -> id);
		}

		final String template = "  - %-" + mxLen + "s = %s";
		final AtomicInteger unknownModels = new AtomicInteger(0);
		final Map<String, Set<ProjectExecutionModel>> index = new LinkedHashMap<>();

		overrides.forEach((id, boolValue, modelSet) -> {
			final String key = refMap.getOrDefault(id, id);
			final String value = boolValue == null ? null : Boolean.toString(boolValue.get());

			if (key == null || value == null)
				getLog().warn(String.format(template, key, value));
			else
				getLog().info(String.format(template, key, value));

			if (modelSet == null)
				return;
			// group by projectId
			for (ProjectExecutionModel model : modelSet) {
				if (model == null)
					continue;
				final ModelSource source = model.getModelSource();
				if (source == null) {
					unknownModels.incrementAndGet();
					continue;
				}

				index.computeIfAbsent(artifactIndex.getOrDefault(source.getPluginId(), source.getProjectId()),
						k -> new LinkedHashSet<>())
						.add(model);
			}
		});

		// log matching models
		if (index.isEmpty()) {
			return;
		}

		getLog().info("from");

		final MavenProject topLevelMvnProject = this.mvnSession.getTopLevelProject();
		final Path basedir = topLevelMvnProject == null ? null
				: topLevelMvnProject.getBasedir()
						.toPath();

		logEntry(basedir, index.remove(ModelSource.id(mvnProject::getGroupId, mvnProject::getArtifactId)), "", "»");

		final String prjGroupId = mvnProject.getGroupId();
		for (Entry<String, Set<ProjectExecutionModel>> entry : index.entrySet()) {
			String projectId = entry.getKey();
			final int idx = projectId.indexOf(':');
			// hide groupId
			if (-1 < idx && prjGroupId != null && prjGroupId.equals(projectId.substring(0, idx)))
				projectId = projectId.substring(idx + 1);

			getLog().info(String.format("  » Project:  %s", projectId));
			logEntry(basedir, entry.getValue(), "  ", "-");
		}

		if (0 < unknownModels.get()) {
			getLog().warn(String.format("  » %i models of unknown origin!", unknownModels.get()));
		}
	}

	private void logEntry(final Path basedir, final Set<ProjectExecutionModel> modelSet, final String offset,
			final String paraFlag) {
		if (modelSet == null || modelSet.isEmpty())
			return;

		for (ProjectExecutionModel model : modelSet) {
			// All models declaring effective-pem status are flagged if they are not
			// user-defined, as this is almost impossible to be discovered!
			// Furthermore declaring models as effective-pem is generally discouraged!

			final boolean danger = model.isEffective() && !model.isUserDefined();

			logModelSource(danger ? //
					getLog()::warn : getLog()::info, //
					offset, paraFlag, basedir, model.getModelSource() //
			);
		}
	}

	private void logModelSource(final Consumer<String> logFnc, final String offset, final String paraFlag,
			final Path basedir, final ModelSource source) {
		if (source == null)
			return;
		boolean start = true;

		final String pluginId = source.getPluginId();
		if (!isBlank(pluginId)) {
			logFnc.accept(String.format("  %s%s Plugin: %s", offset, start ? paraFlag : " ", pluginId));
			start = false;
		}

		Path file = source.getFile();
		if (file != null) {
			if (basedir != null)
				file = basedir.relativize(file);

			logFnc.accept(String.format("  %s%s File:   %s", offset, start ? paraFlag : " ", file.toString()));
			start = false;
		}

		final String note = source.getNote();
		if (!isBlank(note)) {
			logFnc.accept(String.format("  %s%s Note:   %s", offset, start ? paraFlag : " ", note));
			start = false;
		}
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
