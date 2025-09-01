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
package net.runeduniverse.tools.maven.r4m.pem.overrides;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.lib.utils.common.api.DataMap;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSectorSnapshot;
import net.runeduniverse.tools.maven.r4m.pem.api.ModelPredicate;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelCompatProjectParser;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelOverrideFilterSupplier;
import net.runeduniverse.tools.maven.r4m.pem.model.DisableProjectPomOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

import static net.runeduniverse.lib.utils.common.ComparisonUtils.typeIsAssignable;

@Component(role = ProjectExecutionModelOverrideFilterSupplier.class, hint = DisableProjectPomOverride.TYPE)
public class DisableProjectPomFilterSupplier implements ProjectExecutionModelOverrideFilterSupplier {

	@Override
	public ModelPredicate<ProjectExecutionModel, Execution> get(
			final DataMap<String, AtomicBoolean, ExecutionArchiveSectorSnapshot.Data> overrides) {
		final AtomicBoolean value = overrides.get(DisableProjectPomOverride.TYPE);
		if (value == null || !value.get())
			return null;

		final ExecutionArchiveSectorSnapshot.Data data = overrides.getData(DisableProjectPomOverride.TYPE);
		if (data == null)
			return null;

		final Set<ModelOverride> set = data.getModelOverrides();
		if (set == null || set.isEmpty())
			return null;

		final Set<String> ids = new LinkedHashSet<>(1);
		for (ModelOverride modelOverride : set) {
			if (!(modelOverride instanceof DisableProjectPomOverride))
				continue;
			final DisableProjectPomOverride override = (DisableProjectPomOverride) modelOverride;
			if (!override.isActive() || !override.validate())
				continue;
			ids.add(ModelSource.id(override::getGroupId, override::getArtifactId));
		}

		if (ids.isEmpty())
			return null;

		return (pem, exec) -> {
			if (pem == null)
				return false;
			final ModelSource source = pem.getModelSource();
			if (source == null //
					|| !typeIsAssignable(ProjectExecutionModelCompatProjectParser.class, pem.getParserType())
					|| !"plugin-execution".equals(pem.getParserHint()))
				return true;
			return !ids.contains(source.getProjectId());
		};
	}

}
