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

import java.util.LinkedList;
import java.util.List;

import net.runeduniverse.lib.utils.conditions.DefaultConditionInfo;
import net.runeduniverse.tools.maven.r4m.grm.model.LifecycleData;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;
import net.runeduniverse.tools.maven.r4m.grm.view.api.RuntimeView;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

public class LifecycleCheck extends ACheck {

	protected LifecycleData data = null;

	public LifecycleCheck(final String type) {
		super(type);
	}

	public void setData(final LifecycleData data) {
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
		return getId().equals(view.getLifecycleId());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof LifecycleCheck))
			return false;
		final LifecycleCheck other = (LifecycleCheck) obj;
		if (this.data == other.data)
			return true;
		if (this.data == null || other.data == null)
			return false;
		return this.data.equals(other.data);
	}

	@Override
	public List<ConditionInfo> getInfo() {
		final List<ConditionInfo> lst = new LinkedList<>();
		if (this.data != null) {
			lst.add(new DefaultConditionInfo("id", getId()));
		}
		return lst;
	}
}
