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
package net.runeduniverse.tools.maven.r4m.grm.builder.check;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.grm.builder.api.AbstractCheckFactory;
import net.runeduniverse.tools.maven.r4m.grm.builder.api.CheckFactory;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.WhenDataGroup;

@Component(role = CheckFactory.class, hint = WhenDataGroup.HINT)
public class WhenCheckFactory implements CheckFactory {

	@Requirement(role = AbstractCheckFactory.class)
	protected AbstractCheckFactory factory;

	@Override
	public DataEntry createEntry(PlexusConfiguration cnf) {
		if (!WhenDataGroup.HINT.equals(cnf.getName()))
			return null;

		final WhenDataGroup group = new WhenDataGroup();

		for (PlexusConfiguration child : cnf.getChildren()) {
			if (child == null)
				continue;
			final DataEntry entry = factory.createEntry(child);
			if (entry == null)
				continue;
			group.addEntry(entry);
		}

		return group;
	}
}
