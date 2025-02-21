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

import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;
import net.runeduniverse.tools.maven.r4m.grm.view.api.RuntimeView;

public class PhaseIdCheck extends ACheck {

	protected String phaseId = null;

	public PhaseIdCheck(final String type) {
		super(type);
	}

	public String getPhaseId() {
		return this.phaseId;
	}

	public void setPhaseId(String phaseId) {
		this.phaseId = phaseId;
	}

	@Override
	public boolean isValid() {
		return this.phaseId != null;
	}

	@Override
	protected DataCheck<EntityView> check() {
		return and(nonNull(), runtime(and(nonNull(), this::eval)));
	}

	protected boolean eval(RuntimeView data) {
		return this.phaseId.equals(data.getPhaseId());
	}
}
