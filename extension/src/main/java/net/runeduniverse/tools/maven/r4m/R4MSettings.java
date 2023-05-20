package net.runeduniverse.tools.maven.r4m;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.api.Property;
import net.runeduniverse.tools.maven.r4m.api.Settings;

@Component(role = Settings.class, instantiationStrategy = "keep-alive")
public class R4MSettings implements Settings {

	private Property<String> phaseSequenceCalculator = null;

	@Override
	public Property<String> getPhaseSequenceCalculator() {
		return this.phaseSequenceCalculator;
	}

	@Override
	public void setPhaseSequenceCalculator(Property<String> value) {
		this.phaseSequenceCalculator = value;
	}

}
