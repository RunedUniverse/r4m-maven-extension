package net.runeduniverse.tools.runes4tools.maven.r4m.pem.parser;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.model.Plugin;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.ProjectExecutionModelPluginParser;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Execution;

@Component(role = ProjectExecutionModelPluginParser.class, hint = "default")
public class PluginParser implements ProjectExecutionModelPluginParser {

	@Override
	public Set<Execution> parse(Plugin mvnPlugin) {
		// TODO Auto-generated method stub
		return new HashSet<>(0);
	}

}
