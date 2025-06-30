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

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionFilterUtils;
import net.runeduniverse.tools.maven.r4m.pem.api.ModelPredicate;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelOverrideFilterSupplier;
import net.runeduniverse.tools.maven.r4m.pem.model.DeclareSuperPemOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

@Component(role = ProjectExecutionModelOverrideFilterSupplier.class, hint = DeclareSuperPemOverride.HINT)
public class RequireSuperPemFilterSupplier implements ProjectExecutionModelOverrideFilterSupplier {

	@Override
	public ModelPredicate<ProjectExecutionModel, Execution> get(final Map<String, AtomicBoolean> overrides) {
		final AtomicBoolean value = overrides.get(DeclareSuperPemOverride.TYPE);
		if (value == null || !value.get())
			return (pem, e) -> true;

		return ExecutionFilterUtils::requireSuperPemFilter;
	}

}
