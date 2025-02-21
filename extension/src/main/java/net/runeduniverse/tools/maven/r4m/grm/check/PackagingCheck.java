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

import net.runeduniverse.tools.maven.r4m.grm.model.PackagingData;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;
import net.runeduniverse.tools.maven.r4m.grm.view.api.ProjectView;

public class PackagingCheck extends ACheck {

	protected PackagingData data = null;

	public PackagingCheck(final String type) {
		super(type);
	}

	public void setData(final PackagingData data) {
		this.data = data;
	}

	public String getProcedure() {
		return this.data == null ? "" : this.data.getProcedure();
	}

	@Override
	public boolean isValid() {
		if (this.data == null)
			return false;
		return !isBlank(getProcedure());
	}

	@Override
	protected DataCheck<EntityView> check() {
		return and(nonNull(), project(and(nonNull(), this::eval)));
	}

	protected boolean eval(final ProjectView view) {
		return view.getPackagingProcedure()
				.equals(getProcedure());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PackagingCheck))
			return false;
		final PackagingCheck other = (PackagingCheck) obj;
		if (this.data == other.data)
			return true;
		if (this.data == null || other.data == null)
			return false;
		return this.data.equals(other.data);
	}
}
