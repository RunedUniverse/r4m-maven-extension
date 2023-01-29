package net.runeduniverse.tools.maven.r4m.pem.trigger;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;

public class ProfileTriggerMissing extends ProfileTrigger {

	public ProfileTriggerMissing(String profileId) {
		super(profileId);
	}

	@Override
	public boolean isActive(ExecutionArchiveSelectorConfig config) {
		return !config.getProvidedProfiles()
				.contains(this.profileId);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("ProfileTrigger");

		tree.append("is-missing", this.profileId);

		return tree;
	}

}
