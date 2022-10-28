package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal;

import org.apache.maven.plugin.MavenPluginManager;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.model.Plugin;

public class DefaultExecutionArchiveParser implements ExecutionArchiveParser {

	// @Requirement
	private MavenPluginManager pluginManager;

	public DefaultExecutionArchiveParser(MavenPluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public void parsePlugin(ExecutionArchive archive, org.apache.maven.model.Plugin mvnPlugin) {
		if (archive.contains(mvnPlugin))
			return;
		// parse Maven-Plugin -> executions.xml

		XmlStreamReader inputStreamReader;

		//Xpp3Dom executionCfg = Xpp3DomBuilder.build(inputStreamReader, "UTF-8");
		// Xpp3Dom executionCfg = Xpp3DomBuilder.build(inputStreamReader, false);

		// TODO parse
		Plugin plugin = new Plugin(mvnPlugin.getGroupId(), mvnPlugin.getArtifactId(), null);

		archive.register(mvnPlugin, plugin);
	}

}
