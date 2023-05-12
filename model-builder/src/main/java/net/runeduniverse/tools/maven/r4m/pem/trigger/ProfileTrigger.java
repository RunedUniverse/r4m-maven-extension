package net.runeduniverse.tools.maven.r4m.pem.trigger;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;

public abstract class ProfileTrigger implements ExecutionTrigger<ExecutionArchiveSelectorConfig> {

	protected String profileId;

	public ProfileTrigger(String profileId) {
		this.profileId = profileId;
	}

	public String getProfileId() {
		return this.profileId;
	}

}
