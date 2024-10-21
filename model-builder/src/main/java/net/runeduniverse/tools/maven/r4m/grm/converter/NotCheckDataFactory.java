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
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.grm.converter.api.CheckDataConverter;
import net.runeduniverse.tools.maven.r4m.grm.converter.api.CheckDataFactory;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.NotDataGroup;

@Component(role = CheckDataFactory.class, hint = NotDataGroup.HINT)
public class NotCheckDataFactory extends ACheckDataFactory {

	@Requirement(role = CheckDataConverter.class)
	protected CheckDataConverter factory;

	@Override
	public DataEntry createEntry(PlexusConfiguration cnf) {
		if (!NotDataGroup.HINT.equals(cnf.getName()))
			return null;

		final NotDataGroup group = new NotDataGroup();

		addConvertedEntries(group, cnf.getChildren());

		return group;
	}
}
