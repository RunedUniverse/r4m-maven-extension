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
package net.runeduniverse.tools.maven.r4m.grm.converter;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.lib.utils.common.StringUtils;
import net.runeduniverse.tools.maven.r4m.grm.converter.api.DataHandler;
import net.runeduniverse.tools.maven.r4m.grm.converter.api.ConfigurationFactory;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.ProfileData;

@Component(role = DataHandler.class, hint = ProfileData.CANONICAL_NAME)
public class ProfileDataHandler extends ADataHandler {

	@Override
	protected PlexusConfiguration toConfig(final ConfigurationFactory<PlexusConfiguration> factory,
			final DataEntry entry) {
		if (!(entry instanceof ProfileData))
			return null;

		final ProfileData data = (ProfileData) entry;
		final PlexusConfiguration cnf = factory.create(ProfileData.HINT);

		setAttributeAsId(cnf, "id", data.getId());
		final String state = data.getState();
		if (!StringUtils.isBlank(state))
			setAttributeAsId(cnf, "state", state);

		return cnf;
	}
}
