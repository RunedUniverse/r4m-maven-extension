package net.runeduniverse.tools.maven.r4m.api;

import java.util.Collection;

public interface Settings {

	public Collection<Property<?>> getAllProperties();

	public void selectDefaults();

	public Property<String> getPhaseSequenceCalculator();

	public Property<String> getMissingBuildPluginHandler();

	public Property<Boolean> getPatchMojoOnFork();

	public void setPhaseSequenceCalculator(Property<String> value);

	public void setMissingBuildPluginHandler(Property<String> value);

	public void setPatchMojoOnFork(Property<Boolean> value);

}
