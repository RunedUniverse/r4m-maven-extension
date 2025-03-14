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

import net.runeduniverse.lib.utils.conditional.api.Condition;
import net.runeduniverse.tools.maven.r4m.grm.check.PhaseCheck;
import net.runeduniverse.tools.maven.r4m.grm.check.converter.api.CheckFactory;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.PhaseData;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;

@Component(role = CheckFactory.class, hint = PhaseData.CANONICAL_NAME)
public class PhaseCheckFactory implements CheckFactory {

	@Override
	public Condition<EntityView> createCheck(final DataEntry entry) {
		if (!(entry instanceof PhaseData))
			return null;
		final PhaseData data = (PhaseData) entry;
		final PhaseCheck check = new PhaseCheck(data.type());
		check.setData(data);
		return check;
	}
}
