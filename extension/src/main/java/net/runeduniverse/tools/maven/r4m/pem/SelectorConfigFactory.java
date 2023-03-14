package net.runeduniverse.tools.maven.r4m.pem;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfigFactory;

@Component(role = ExecutionArchiveSelectorConfigFactory.class, hint = "default", instantiationStrategy = "singleton")
public class SelectorConfigFactory implements ExecutionArchiveSelectorConfigFactory {

	@Override
	public ExecutionArchiveSelectorConfig createEmptyConfig() {
		return new SelectorConfig();
	}

}