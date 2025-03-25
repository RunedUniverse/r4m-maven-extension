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
package net.runeduniverse.tools.maven.r4m.pem.trigger;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;

public class ProfileTriggerActive extends ProfileTrigger {

	public static final String HINT = "active-profile";

	public ProfileTriggerActive(final String profileId) {
		super(profileId);
	}

	@Override
	public String getHint() {
		return HINT;
	}

	@Override
	public boolean isActive(final ExecutionArchiveSelectorConfig config) {
		return config.getActiveProfiles()
				.contains(this.profileId);
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("ProfileTrigger");

		tree.append("is-active", this.profileId);

		return tree;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ProfileTriggerActive))
			return false;
		final ProfileTriggerActive trigger = (ProfileTriggerActive) obj;
		if (getProfileId() == null)
			return trigger.getProfileId() == null;
		return getProfileId().equals(trigger.getProfileId());
	}

}
