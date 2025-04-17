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
package net.runeduniverse.tools.maven.r4m.pem.converter;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataConverter;
import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataFactory;
import net.runeduniverse.tools.maven.r4m.pem.model.DataEntry;

public abstract class ADataFactory implements DataFactory {

	@Requirement(role = DataConverter.class)
	protected DataConverter converter;

	protected DataEntry convertEntry(final PlexusConfiguration cnf) {
		return this.converter.convertEntry(null, cnf);
	}

	protected DataEntry convertEntry(final String contextHint, final PlexusConfiguration cnf) {
		return this.converter.convertEntry(null, cnf);
	}

	protected String asId(final String id) {
		if (id == null)
			return null;
		return id.trim();
	}

	protected String getAttributeAsId(final PlexusConfiguration cnf, final String attributeName) {
		if (cnf == null)
			return null;
		return asId(cnf.getAttribute(attributeName));
	}

	protected String getAttributeAsId(final PlexusConfiguration cnf, final String attributeName,
			final String defaultValue) {
		if (cnf == null)
			return null;
		return asId(cnf.getAttribute(attributeName, defaultValue));
	}

	protected String getValueAsId(final PlexusConfiguration cnf) {
		if (cnf == null)
			return null;
		return asId(cnf.getValue());
	}

	protected String getValueAsId(final PlexusConfiguration cnf, final String defaultValue) {
		if (cnf == null)
			return null;
		return asId(cnf.getValue(defaultValue));
	}
}
