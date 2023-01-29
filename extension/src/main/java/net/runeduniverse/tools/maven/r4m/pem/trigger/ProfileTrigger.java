package net.runeduniverse.tools.maven.r4m.pem.trigger;

import net.runeduniverse.tools.maven.r4m.api.pem.model.Trigger;

public abstract class ProfileTrigger implements Trigger {
	protected String profileId;

	public ProfileTrigger(String profileId) {
		this.profileId = profileId;
	}

	public String getProfileId() {
		return this.profileId;
	}
}
