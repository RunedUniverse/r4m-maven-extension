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

import java.util.Map;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.converter.api.ConfigurationFactory;
import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataConverter;
import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataFactory;
import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataHandler;
import net.runeduniverse.tools.maven.r4m.pem.model.DataEntry;

import static net.runeduniverse.lib.utils.common.ReflectionUtils.supplyWithHandler;

@Component(role = DataConverter.class, hint = "default")
public class DefaultDataConverter implements DataConverter {

	public static int MAX_TYPE_SEARCH_DEPTH = 4;

	@Requirement(role = DataFactory.class)
	protected Map<String, DataFactory> factories;
	@Requirement(role = DataHandler.class)
	protected Map<String, DataHandler> handler;

	@Override
	public DataEntry convertEntry(final String contextHint, final PlexusConfiguration cnf) {
		if (cnf == null)
			return null;
		final String hint = (contextHint == null ? "" : contextHint + '>') + cnf.getName();
		final DataFactory factory = this.factories.get(hint);
		if (factory == null)
			return null;
		return factory.createEntry(cnf);
	}

	@Override
	public PlexusConfiguration convertEntry(final ConfigurationFactory<PlexusConfiguration> factory,
			final DataEntry entry) {
		if (factory == null || entry == null)
			return null;

		return supplyWithHandler(this.handler, entry, h -> h.createConfiguration(factory, entry),
				MAX_TYPE_SEARCH_DEPTH);
	}
}
