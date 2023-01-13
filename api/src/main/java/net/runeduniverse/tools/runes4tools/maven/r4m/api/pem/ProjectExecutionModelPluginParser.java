package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem;

import java.util.Set;

import org.apache.maven.model.Plugin;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Execution;

public interface ProjectExecutionModelPluginParser {

	Set<Execution> parse(Plugin mvnPlugin);

}
