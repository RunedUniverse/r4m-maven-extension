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
package net.runeduniverse.tools.maven.r4m.grm.check;

import java.util.Collection;
import java.util.LinkedHashSet;

import net.runeduniverse.lib.utils.conditional.AndCondition;
import net.runeduniverse.lib.utils.conditional.api.Condition;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;

public class AndCheck extends AndCondition<EntityView> {

	private final String type;

	public AndCheck(final String type) {
		super(new LinkedHashSet<>());
		this.type = type;
	}

	public AndCheck(final String type, final Collection<Condition<EntityView>> conditions) {
		super(conditions);
		this.type = type;
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AndCheck))
			return false;
		return super.equals(obj);
	}
}
