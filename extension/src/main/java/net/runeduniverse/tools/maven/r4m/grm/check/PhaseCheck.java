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
package net.runeduniverse.tools.maven.r4m.grm.check;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import net.runeduniverse.tools.maven.r4m.grm.model.PhaseData;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;
import net.runeduniverse.tools.maven.r4m.grm.view.api.RuntimeView;

public class PhaseCheck extends ACheck {

	protected PhaseData data = null;

	public PhaseCheck(final String type) {
		super(type);
	}

	public void setData(final PhaseData data) {
		this.data = data;
	}

	public String getId() {
		return this.data == null ? "" : this.data.getId();
	}

	@Override
	public boolean isValid() {
		if (this.data == null)
			return false;
		return !isBlank(getId());
	}

	@Override
	protected DataCheck<EntityView> check() {
		return and(nonNull(), runtime(and(nonNull(), this::eval)));
	}

	protected boolean eval(final RuntimeView view) {
		return getId().equals(view.getPhaseId());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PhaseCheck))
			return false;
		final PhaseCheck other = (PhaseCheck) obj;
		if (this.data == other.data)
			return true;
		if (this.data == null || other.data == null)
			return false;
		return this.data.equals(other.data);
	}
}
