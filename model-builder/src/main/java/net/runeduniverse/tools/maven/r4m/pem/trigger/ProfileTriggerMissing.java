package net.runeduniverse.tools.maven.r4m.pem.trigger;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;

public class ProfileTriggerMissing extends ProfileTrigger {

	public static final String HINT = "missing-profile";

	public ProfileTriggerMissing(String profileId) {
		super(profileId);
	}

	@Override
	public String getHint() {
		return HINT;
	}

	@Override
	public boolean isActive(final ExecutionArchiveSelectorConfig config) {
		return !config.getProvidedProfiles()
				.contains(this.profileId);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("ProfileTrigger");

		tree.append("is-missing", this.profileId);

		return tree;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ProfileTriggerMissing))
			return false;
		ProfileTriggerMissing trigger = (ProfileTriggerMissing) obj;
		if (getProfileId() == null)
			if (trigger.getProfileId() != null)
				return false;
		return getProfileId().equals(trigger.getProfileId());
	}
}
