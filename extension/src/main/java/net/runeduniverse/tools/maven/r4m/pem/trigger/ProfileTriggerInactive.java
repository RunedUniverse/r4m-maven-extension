package net.runeduniverse.tools.maven.r4m.pem.trigger;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;

public class ProfileTriggerInactive extends ProfileTrigger {

	public ProfileTriggerInactive(String profileId) {
		super(profileId);
	}

	@Override
	public boolean isActive(ExecutionArchiveSelectorConfig config) {
		return config.getInactiveProfiles()
				.contains(this.profileId);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("ProfileTrigger");

		tree.append("is-inactive", this.profileId);

		return tree;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ProfileTriggerInactive))
			return false;
		ProfileTriggerInactive trigger = (ProfileTriggerInactive) obj;
		if (getProfileId() == null)
			if (trigger.getProfileId() != null)
				return false;
		return getProfileId().equals(trigger.getProfileId());
	}
}
