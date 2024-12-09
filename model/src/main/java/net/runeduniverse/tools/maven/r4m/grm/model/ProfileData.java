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
package net.runeduniverse.tools.maven.r4m.grm.model;

import static net.runeduniverse.tools.maven.r4m.grm.model.ModelUtils.hash;
import static net.runeduniverse.tools.maven.r4m.grm.model.ModelUtils.strEquals;

public class ProfileData implements DataEntry {

	public static final String HINT = "profile";
	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.grm.model.ProfileData";

	public static final String STATE_ACTIVE = "active";
	public static final String STATE_INACTIVE = "inactive";
	public static final String STATE_MISSING = "missing";
	public static final String STATE_PROVIDED = "provided";

	protected String id = null;
	protected String state = null;

	public String getId() {
		return this.id;
	}

	public String getState() {
		return this.state;
	}

	public ProfileData setId(final String id) {
		this.id = id;
		return this;
	}

	public ProfileData setState(final String state) {
		this.state = state;
		return this;
	}

	@Override
	public DataEntry copy() {
		return new ProfileData().setId(this.id)
				.setState(this.state);
	}

	@Override
	public int hashCode() {
		return hash(type()) ^ hash(getId()) ^ hash(getState());
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || hashCode() != obj.hashCode() || !(obj instanceof ProfileData))
			return false;
		final ProfileData other = (ProfileData) obj;
		return strEquals(getId(), other.getId()) && strEquals(getState(), other.getState());
	}
}
