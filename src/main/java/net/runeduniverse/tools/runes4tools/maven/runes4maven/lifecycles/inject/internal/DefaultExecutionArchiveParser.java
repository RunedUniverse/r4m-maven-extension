package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal;

import org.apache.maven.plugin.MavenPluginManager;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model.Plugin;

public class DefaultExecutionArchiveParser implements ExecutionArchiveParser {

	private MavenPluginManager pluginManager;

	public DefaultExecutionArchiveParser(MavenPluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public void parsePlugin(ExecutionArchive archive, org.apache.maven.model.Plugin mvnPlugin) {
		if (archive.contains(mvnPlugin))
			return;
		// parse Maven-Plugin -> executions.xml

		// TODO parse
		Plugin plugin = new Plugin(null, null, null);

		archive.register(mvnPlugin, plugin);
	}

}
