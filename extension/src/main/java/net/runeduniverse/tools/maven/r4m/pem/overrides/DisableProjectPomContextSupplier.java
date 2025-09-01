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

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelOverrideContextSupplier;
import net.runeduniverse.tools.maven.r4m.pem.model.DisableProjectPomOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelSource;

@Component(role = ProjectExecutionModelOverrideContextSupplier.class, hint = DisableProjectPomOverride.TYPE)
public class DisableProjectPomContextSupplier implements ProjectExecutionModelOverrideContextSupplier {

	@Override
	public String get(final ModelOverride modelOverride) {
		if (!(modelOverride instanceof DisableProjectPomOverride))
			return null;
		final DisableProjectPomOverride override = (DisableProjectPomOverride) modelOverride;

		return "Project: " + ModelSource.id(override::getGroupId, override::getArtifactId);
	}

}
