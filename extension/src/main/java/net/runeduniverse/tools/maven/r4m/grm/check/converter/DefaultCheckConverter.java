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
package net.runeduniverse.tools.maven.r4m.grm.check.converter;

import java.util.Map;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import net.runeduniverse.lib.utils.conditional.api.Condition;
import net.runeduniverse.tools.maven.r4m.grm.check.converter.api.CheckConverter;
import net.runeduniverse.tools.maven.r4m.grm.check.converter.api.CheckFactory;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;

@Component(role = CheckConverter.class, hint = "default")
public class DefaultCheckConverter implements CheckConverter {

	public static int MAX_TYPE_SEARCH_DEPTH = 4;

	@Requirement(role = CheckFactory.class)
	protected Map<String, CheckFactory> factories;

	@Override
	public Condition<EntityView> convertEntry(final DataEntry entry) {
		if (entry == null)
			return null;

		Class<?> clazz = entry.getClass();
		Condition<EntityView> con = null;

		for (int i = 0; i <= MAX_TYPE_SEARCH_DEPTH; i++) {
			if (clazz == Object.class)
				return null;
			// test class
			con = convertEntryWithType(entry, clazz.getCanonicalName());
			if (con != null)
				return con;
			// test all interfaces
			for (Class<?> ic : clazz.getInterfaces()) {
				con = convertEntryWithType(entry, ic.getCanonicalName());
				if (con != null)
					return con;
			}
			// try again with the superclass
			clazz = clazz.getSuperclass();
		}

		return null;
	}

	protected Condition<EntityView> convertEntryWithType(final DataEntry entry, final String canonicalName) {
		// find valid handler
		final CheckFactory factory = this.factories.get(canonicalName);
		if (factory == null)
			return null;
		// if the factory rejects the entry null is returned
		return factory.createCheck(entry);
	}
}