/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.pem.converter;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataFactory;
import net.runeduniverse.tools.maven.r4m.pem.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.pem.model.DisableProjectPomOverride;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelOverride;

@Component(role = DataFactory.class, hint = ModelOverride.CONTEXT + '>' + DisableProjectPomOverride.HINT)
public class DisableProjectPomOverrideFactory extends ADataFactory {

	@Override
	public DataEntry createEntry(final PlexusConfiguration cnf) {
		if (!DisableProjectPomOverride.HINT.equals(cnf.getName()))
			return null;

		final String groupId = getValueAsId(cnf.getChild("groupId"));
		final String artifactId = getValueAsId(cnf.getChild("artifactId"));

		Boolean active = getAttributeAsBoolean(cnf, "value", true);
		if (active == null)
			active = true;
		return new DisableProjectPomOverride(groupId, artifactId, active);
	}
}
