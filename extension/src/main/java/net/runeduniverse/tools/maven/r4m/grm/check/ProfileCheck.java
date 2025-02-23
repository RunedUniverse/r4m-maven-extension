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

import net.runeduniverse.tools.maven.r4m.grm.model.ProfileData;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;
import net.runeduniverse.tools.maven.r4m.grm.view.api.RuntimeView;

import java.util.LinkedList;
import java.util.List;

import net.runeduniverse.lib.utils.conditions.DefaultConditionInfo;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

public class ProfileCheck extends ACheck {

	protected ProfileData data = null;

	public ProfileCheck(final String type) {
		super(type);
	}

	public void setData(final ProfileData data) {
		this.data = data;
	}

	public String getId() {
		return this.data == null ? "" : this.data.getId();
	}

	public String getState() {
		return this.data == null ? "" : this.data.getState();
	}

	@Override
	public boolean isValid() {
		if (this.data == null)
			return false;
		return !(isBlank(getId()) || isBlank(getState()));
	}

	@Override
	protected DataCheck<EntityView> check() {
		return and(nonNull(), runtime(and(nonNull(), this::eval)));
	}

	protected boolean eval(final RuntimeView view) {
		switch (getState()) {
		case ProfileData.STATE_ACTIVE:
			return view.getActiveProfileIds()
					.contains(getId());
		case ProfileData.STATE_INACTIVE:
			return view.getInactiveProfileIds()
					.contains(getId());
		case ProfileData.STATE_PROVIDED:
			return view.getProvidedProfileIds()
					.contains(getId());
		case ProfileData.STATE_MISSING:
			return !view.getProvidedProfileIds()
					.contains(getId());
		}
		return false;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ProfileCheck))
			return false;
		final ProfileCheck other = (ProfileCheck) obj;
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
			lst.add(new DefaultConditionInfo("state", getState()));
		}
		return lst;
	}
}
