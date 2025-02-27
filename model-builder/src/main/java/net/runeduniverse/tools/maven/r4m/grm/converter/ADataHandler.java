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

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.grm.converter.api.ConfigurationFactory;
import net.runeduniverse.tools.maven.r4m.grm.converter.api.DataConverter;
import net.runeduniverse.tools.maven.r4m.grm.converter.api.DataHandler;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.DataGroup;

public abstract class ADataHandler implements DataHandler {

	@Requirement(role = DataConverter.class)
	protected DataConverter converter;

	@Override
	public PlexusConfiguration createConfiguration(final ConfigurationFactory<PlexusConfiguration> factory,
			final DataEntry entry) {
		if (factory == null || entry == null)
			return null;
		return toConfig(factory, entry);
	}

	protected abstract PlexusConfiguration toConfig(final ConfigurationFactory<PlexusConfiguration> factory,
			final DataEntry entry);

	protected PlexusConfiguration convertEntry(final ConfigurationFactory<PlexusConfiguration> factory,
			final DataEntry entry) {
		return this.converter.convertEntry(factory, entry);
	}

	protected boolean addConvertedEntry(final PlexusConfiguration cnf,
			final ConfigurationFactory<PlexusConfiguration> factory, final DataEntry entry) {
		final PlexusConfiguration entryCnf = convertEntry(factory, entry);
		if (entryCnf == null)
			return false;
		cnf.addChild(entryCnf);
		return true;
	}

	protected void addConvertedEntries(final PlexusConfiguration cnf,
			final ConfigurationFactory<PlexusConfiguration> factory, final DataGroup group) {
		for (DataEntry entry : group.getEntries())
			addConvertedEntry(cnf, factory, entry);
	}

	protected String asId(final String id) {
		if (id == null)
			return null;
		return id.trim();
	}

	protected void setAttributeAsId(final PlexusConfiguration cnf, final String attributeName, final String value) {
		cnf.setAttribute(attributeName, asId(value));
	}

	protected void setValueAsId(final PlexusConfiguration cnf, final String value) {
		cnf.setValue(asId(value));
	}
}
