package net.runeduniverse.tools.maven.r4m.pem.trigger;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;

public class ProfileTriggerActive extends ProfileTrigger {

	public ProfileTriggerActive(String id) {
		super(id);
	}

	@Override
	public boolean isActive(ExecutionArchiveSelectorConfig config) {
		return config.getActiveProfiles()
				.contains(this.profileId);
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("ProfileTrigger");

		tree.append("is-active", this.profileId);

		return tree;
	}

}
