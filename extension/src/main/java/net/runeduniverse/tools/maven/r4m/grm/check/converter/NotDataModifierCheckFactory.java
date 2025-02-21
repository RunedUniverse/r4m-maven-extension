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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.lib.utils.conditions.api.Condition;
import net.runeduniverse.tools.maven.r4m.grm.check.NotCheck;
import net.runeduniverse.tools.maven.r4m.grm.check.converter.api.CheckConverter;
import net.runeduniverse.tools.maven.r4m.grm.check.converter.api.CheckFactory;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.NotDataModifier;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;

@Component(role = CheckFactory.class, hint = NotDataModifier.CANONICAL_NAME)
public class NotDataModifierCheckFactory extends AModifierCheckFactory {

	@Requirement
	protected CheckConverter converter;

	@Override
	public Condition<EntityView> createCheck(final DataEntry entry) {
		if (!(entry instanceof NotDataModifier))
			return null;
		final NotDataModifier data = (NotDataModifier) entry;
		final NotCheck check = new NotCheck();

		convertEntry(data, check);

		return check;
	}
}
