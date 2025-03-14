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
package net.runeduniverse.tools.maven.r4m.grm.check.converter;

import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.lib.utils.conditional.api.ConditionModifier;
import net.runeduniverse.tools.maven.r4m.grm.check.converter.api.CheckConverter;
import net.runeduniverse.tools.maven.r4m.grm.check.converter.api.CheckFactory;
import net.runeduniverse.tools.maven.r4m.grm.model.DataModifier;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;

public abstract class AModifierCheckFactory implements CheckFactory {

	@Requirement
	protected CheckConverter converter;

	protected void convertEntry(final DataModifier data, final ConditionModifier<EntityView> check) {
		check.setCondition(this.converter.convertEntry(data.getEntry()));
	}
}
