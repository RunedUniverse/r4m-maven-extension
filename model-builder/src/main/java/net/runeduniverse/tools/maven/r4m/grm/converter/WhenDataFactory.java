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
package net.runeduniverse.tools.maven.r4m.grm.converter;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.grm.converter.api.DataConverter;
import net.runeduniverse.tools.maven.r4m.grm.converter.api.DataFactory;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.DataGroup;
import net.runeduniverse.tools.maven.r4m.grm.model.WhenDataGroup;

@Component(role = DataFactory.class, hint = WhenDataGroup.HINT)
public class WhenDataFactory extends ADataFactory {

	@Requirement(role = DataConverter.class)
	protected DataConverter factory;

	@Override
	public DataEntry createEntry(final PlexusConfiguration cnf) {
		if (!WhenDataGroup.HINT.equals(cnf.getName()))
			return null;

		final WhenDataGroup group = new WhenDataGroup();

		addConvertedEntries(group, cnf.getChildren());

		return group;
	}

	@Override
	protected boolean addConvertedEntry(final DataGroup group, final PlexusConfiguration cnf) {
		if (group instanceof WhenDataGroup) {
			final WhenDataGroup when = (WhenDataGroup) group;
			if ("always".equals(cnf.getName())) {
				when.setAlwaysActive(true);
				return true;
			}
			if ("never".equals(cnf.getName())) {
				when.setNeverActive(true);
				return true;
			}
		}
		return super.addConvertedEntry(group, cnf);
	}
}
