package net.runeduniverse.tools.maven.r4m;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.api.Property;
import net.runeduniverse.tools.maven.r4m.api.Settings;

@Component(role = Settings.class, instantiationStrategy = "keep-alive")
public class R4MSettings implements Settings {

	private final HashSet<Property<?>> properties = new LinkedHashSet<>();

	private Property<String> phaseSequenceCalculator = null;
	private Property<String> missingBuildPluginHandler = null;
	private Property<Boolean> patchMojoOnFork = null;
	private Property<Boolean> generatePluginExecutions = null;
	private Property<Boolean> generatePluginExecutionsOnFork = null;

	@Override
	public Collection<Property<?>> getAllProperties() {
		return this.properties;
	}

	@Override
	public void selectDefaults() {
		for (Property<?> property : properties)
			selectDefault(property);
	}

	public <T> void selectDefault(Property<T> property) {
		if (property == null)
			return;
		if (property.getSelected() == null)
			property.setSelected(property.getDefault());
	}

	//////////////////////////////////////////////////////////////////////////

	@Override
	public Property<String> getPhaseSequenceCalculator() {
		return this.phaseSequenceCalculator;
	}

	@Override
	public Property<String> getMissingBuildPluginHandler() {
		return this.missingBuildPluginHandler;
	}

	@Override
	public Property<Boolean> getPatchMojoOnFork() {
		return this.patchMojoOnFork;
	}

	@Override
	public Property<Boolean> getGeneratePluginExecutions() {
		return this.generatePluginExecutions;
	}

	@Override
	public Property<Boolean> getGeneratePluginExecutionsOnFork() {
		return this.generatePluginExecutionsOnFork;
	}

	//////////////////////////////////////////////////////////////////////////

	@Override
	public void setPhaseSequenceCalculator(Property<String> value) {
		if (value == null)
			this.properties.remove(this.phaseSequenceCalculator);
		else
			this.properties.add(value);
		this.phaseSequenceCalculator = value;
	}

	@Override
	public void setMissingBuildPluginHandler(Property<String> value) {
		if (value == null)
			this.properties.remove(this.missingBuildPluginHandler);
		else
			this.properties.add(value);
		this.missingBuildPluginHandler = value;
	}

	@Override
	public void setPatchMojoOnFork(Property<Boolean> value) {
		if (value == null)
			this.properties.remove(this.patchMojoOnFork);
		else
			this.properties.add(value);
		this.patchMojoOnFork = value;
	}

	@Override
	public void setGeneratePluginExecutions(Property<Boolean> value) {
		if (value == null)
			this.properties.remove(this.generatePluginExecutions);
		else
			this.properties.add(value);
		this.generatePluginExecutions = value;
	}

	@Override
	public void setGeneratePluginExecutionsOnFork(Property<Boolean> value) {
		if (value == null)
			this.properties.remove(this.generatePluginExecutionsOnFork);
		else
			this.properties.add(value);
		this.generatePluginExecutionsOnFork = value;
	}

}
