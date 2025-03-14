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
package net.runeduniverse.tools.maven.r4m.pem.trigger;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;

public class ProfileTriggerInactive extends ProfileTrigger {

	public static final String HINT = "inactive-profile";

	public ProfileTriggerInactive(String profileId) {
		super(profileId);
	}

	@Override
	public String getHint() {
		return HINT;
	}

	@Override
	public boolean isActive(final ExecutionArchiveSelectorConfig config) {
		return config.getInactiveProfiles()
				.contains(this.profileId);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new DefaultCompoundTree("ProfileTrigger");

		tree.append("is-inactive", this.profileId);

		return tree;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ProfileTriggerInactive))
			return false;
		final ProfileTriggerInactive trigger = (ProfileTriggerInactive) obj;
		if (getProfileId() == null)
			return trigger.getProfileId() == null;
		return getProfileId().equals(trigger.getProfileId());
	}

}
