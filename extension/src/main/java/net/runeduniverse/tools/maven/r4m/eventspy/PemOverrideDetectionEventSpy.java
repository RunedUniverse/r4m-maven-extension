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
package net.runeduniverse.tools.maven.r4m.eventspy;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.lib.utils.common.api.DataMap;
import net.runeduniverse.tools.maven.r4m.R4MProperties;
import net.runeduniverse.tools.maven.r4m.event.api.ProjectExecutionModelOverrideDetectionEvent;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSectorSnapshot;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelOverrideContextSupplier;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = EventSpy.class, hint = PemOverrideDetectionEventSpy.HINT)
public class PemOverrideDetectionEventSpy implements EventSpy {

	public static final String HINT = R4MProperties.PREFIX_ID + "-eventspy-pem-override-detection";

	@Requirement
	private Logger log;

	@Requirement(role = ProjectExecutionModelOverrideContextSupplier.class)
	protected Map<String, ProjectExecutionModelOverrideContextSupplier> overrideContextSupplier;

	@Override
	public void init(final Context context) throws Exception {
	}

	@Override
	public void onEvent(final Object eventObj) throws Exception {
		if (!(eventObj instanceof ProjectExecutionModelOverrideDetectionEvent))
			return;

		final ProjectExecutionModelOverrideDetectionEvent event = (ProjectExecutionModelOverrideDetectionEvent) eventObj;

		handleModelLogging(event);
	}

	@Override
	public void close() throws Exception {
	}

	private void handleModelLogging(final ProjectExecutionModelOverrideDetectionEvent event) {
		// check validity
		final MavenProject mvnProject = event.getMvnProject();
		final DataMap<String, AtomicBoolean, ExecutionArchiveSectorSnapshot.Data> overridesSrc = event.getOverrides();

		if (mvnProject == null || overridesSrc == null || overridesSrc.isEmpty())
			return;

		final Map<String, AtomicBoolean> overrides = new LinkedHashMap<>();
		final Set<ProjectExecutionModel> modelsSet = new LinkedHashSet<>(0);
		final Map<String, Set<String>> contextMap = new LinkedHashMap<>(0);

		overridesSrc.forEach((k, b, data) -> {
			for (String label : getLabel(contextMap, k, data.getModelOverrides()))
				overrides.put(label, b);
			final Set<ProjectExecutionModel> models = data.getProjectExecutionModels();
			if (models == null)
				return;
			modelsSet.addAll(models);
		});

		// print header
		this.log.info("\033[1mActive Overrides\033[m");

		// log active overrides
		int mxLen = 0;
		for (String id : overrides.keySet()) {
			int len = id == null ? 4 : id.length();
			if (30 < len)
				continue;
			if (mxLen < len)
				mxLen = len;
		}
		final String template = "  - %-" + mxLen + "s = %s";

		for (Entry<String, AtomicBoolean> entry : overrides.entrySet()) {
			final String id = entry.getKey();
			final AtomicBoolean boolValue = entry.getValue();
			final String value = boolValue == null ? null : Boolean.toString(boolValue.get());

			if (id == null || value == null) {
				this.log.warn(String.format(template, id, value));
				for (String context : contextMap.getOrDefault(id, Collections.emptySet()))
					this.log.warn(String.format("      [ %s ]", context));
			} else {
				this.log.info(String.format(template, id, value));
				for (String context : contextMap.getOrDefault(id, Collections.emptySet()))
					this.log.info(String.format("      [ %s ]", context));
			}
		}

		// log matching models
		if (modelsSet == null || modelsSet.isEmpty()) {
			return;
		}

		this.log.info("\033[1mfrom\033[m");

		// index all plugin artifacts in the project tree
		final Map<String, String> artifactIndex = new LinkedHashMap<>();
		for (MavenProject mvnPrj = mvnProject; mvnPrj != null; mvnPrj = mvnPrj.getParent()) {
			final String id = ModelSource.id(mvnPrj::getGroupId, mvnPrj::getArtifactId);
			for (Plugin plugin : mvnPrj.getBuildPlugins())
				artifactIndex.computeIfAbsent(ModelSource.id(plugin::getGroupId, plugin::getArtifactId), k -> id);
		}

		int unknownModels = 0;
		final Map<String, Set<ProjectExecutionModel>> index = new LinkedHashMap<>();

		// group by projectId
		for (ProjectExecutionModel model : modelsSet) {
			if (model == null)
				continue;
			final ModelSource source = model.getModelSource();
			if (source == null) {
				unknownModels = unknownModels + 1;
				continue;
			}

			index.computeIfAbsent(artifactIndex.getOrDefault(source.getPluginId(), source.getProjectId()),
					k -> new LinkedHashSet<>())
					.add(model);
		}

		final MavenProject topLevelMvnProject = event.getTopLevelMvnProject();
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

			this.log.info(String.format("  » Project:  %s", projectId));
			logEntry(basedir, entry.getValue(), "  ", "-");
		}

		if (0 < unknownModels) {
			this.log.warn(String.format("\033[1m  » %i models of unknown origin!\033[m", unknownModels));
		}
	}

	private Set<String> getLabel(final Map<String, Set<String>> contextMap, final String type,
			final Set<ModelOverride> overrides) {
		final Set<String> set = new LinkedHashSet<>(1);
		final ProjectExecutionModelOverrideContextSupplier supplier = this.overrideContextSupplier.get(type);
		if (overrides != null) {
			for (ModelOverride override : overrides) {
				if (override == null)
					continue;
				final String hint = override.hint();
				set.add(hint);
				if (supplier != null) {
					final String context = supplier.get(override);
					if (isBlank(context))
						continue;
					contextMap.computeIfAbsent(hint, k -> new LinkedHashSet<>(1))
							.add(context);
				}
			}
		}
		if (set.isEmpty())
			set.add(type);
		return set;
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
					this.log::warn : this.log::info, //
					offset, paraFlag, basedir, model.getModelSource() //
			);
		}
	}

	private void logModelSource(final Consumer<String> logFnc, final String offset, final String paraFlag,
			final Path basedir, final ModelSource source) {
		if (source == null)
			return;
		boolean start = true;

		final String artifactId = source.getPluginId();
		if (!isBlank(artifactId)) {
			logFnc.accept(String.format("  %s%s Plugin: %s", offset, start ? paraFlag : " ", artifactId));
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

}
