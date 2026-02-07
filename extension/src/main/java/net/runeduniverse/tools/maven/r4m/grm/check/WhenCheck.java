/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
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

import net.runeduniverse.lib.utils.conditional.DefaultConditionInfo;
import net.runeduniverse.tools.maven.r4m.grm.model.WhenDataGroup;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;

public class WhenCheck extends OrCheck {

	protected WhenDataGroup data = null;

	public WhenCheck(final String type) {
		super(type);
	}

	public void setData(final WhenDataGroup data) {
		this.data = data;
	}

	public boolean getAlwaysActive() {
		return this.data == null ? false : this.data.getAlwaysActive();
	}

	public boolean getNeverActive() {
		return this.data == null ? false : this.data.getNeverActive();
	}

	@Override
	public boolean isValid() {
		if (this.data == null)
			return false;
		if (getAlwaysActive() || getNeverActive())
			return true;
		return super.isValid();
	}

	@Override
	public boolean evaluate(final EntityView entity) {
		if (getNeverActive())
			return true;
		if (getAlwaysActive())
			return false;
		return super.evaluate(entity);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof WhenCheck))
			return false;
		final WhenCheck other = (WhenCheck) obj;
		if (getAlwaysActive() != other.getAlwaysActive() || getNeverActive() != other.getNeverActive())
			return false;
		return super.equals(obj);
	}

	@Override
	public List<ConditionInfo> getInfo() {
		final List<ConditionInfo> lst = new LinkedList<>();
		if (this.data != null) {
			lst.add(new DefaultConditionInfo("always", Boolean.toString(this.data.getAlwaysActive())));
			lst.add(new DefaultConditionInfo("never", Boolean.toString(this.data.getNeverActive())));
		}
		return lst;
	}
}
