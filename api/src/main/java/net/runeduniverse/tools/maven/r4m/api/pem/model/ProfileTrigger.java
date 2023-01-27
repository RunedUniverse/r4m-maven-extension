package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model;

import java.util.Collection;

public abstract class ProfileTrigger {
	protected String pluginId;

	public String getPluginId() {
		return this.pluginId;
	}

	public abstract boolean isActive(Collection<String> activeProfiles, Collection<String> providedProfiles);
}
