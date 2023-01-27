package net.runeduniverse.tools.maven.r4m.executions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.api.executions.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.api.executions.ExecutionArchiveParser;
import net.runeduniverse.tools.maven.r4m.api.executions.model.ExecutionDescriptor;
import net.runeduniverse.tools.maven.r4m.api.executions.model.Plugin;
import net.runeduniverse.tools.maven.r4m.errors.ExecutionDescriptorParsingException;

public class ConfigExecutionArchiveParser implements ExecutionArchiveParser {

	public static final String ARCHIVE_PLUGIN_FLAG = "r4m:config-parsed";

	// @Requirement
	private MavenPluginManager pluginManager;

	// @Requirement
	private Logger logger;

	private ExecutionDescriptorBuilder builder = new ExecutionDescriptorBuilder();

	public ConfigExecutionArchiveParser(MavenPluginManager pluginManager, Logger logger) {
		this.pluginManager = pluginManager;
		this.logger = logger;
	}

	public void parsePlugin(final ExecutionArchive archive, final MavenSession mvnSession,
			final MavenProject mvnProject, org.apache.maven.model.Plugin mvnPlugin) {
		if (archive.isRegisteredWithFlag(mvnPlugin, ARCHIVE_PLUGIN_FLAG, true))
			return;

		Plugin plugin = null;
		if (archive.isRegistered(mvnPlugin))
			plugin = archive.getPlugin(mvnPlugin);
		else {
			plugin = new Plugin(mvnPlugin.getGroupId(), mvnPlugin.getArtifactId(), null);
			archive.register(mvnPlugin, plugin);
		}
		// parse Maven-Plugin -> executions.xml
		ExecutionDescriptor executionDescriptor = null;

		try {
			plugin.setDescriptor(pluginManager.getPluginDescriptor(mvnPlugin, mvnProject.getRemotePluginRepositories(),
					mvnSession.getRepositorySession()));
			executionDescriptor = extractExecutionDescriptor(plugin.getDescriptor()
					.getPluginArtifact(), mvnPlugin);
		} catch (PluginResolutionException | PluginDescriptorParsingException | ExecutionDescriptorParsingException
				| InvalidPluginDescriptorException e) {
			e.printStackTrace();
		}
		archive.register(mvnPlugin, plugin);

		if (executionDescriptor == null)
			return;

		logger.debug(plugin.getGroupId() + ':' + plugin.getArtifactId() + " | " + executionDescriptor.toString());
		plugin.setExecutionDescriptor(executionDescriptor);
		archive.setFlag(mvnPlugin, ARCHIVE_PLUGIN_FLAG, true);
	}

	private ExecutionDescriptor extractExecutionDescriptor(Artifact pluginArtifact,
			org.apache.maven.model.Plugin mvnPlugin) throws ExecutionDescriptorParsingException {
		ExecutionDescriptor executionDescriptor = null;
		File pluginFile = pluginArtifact.getFile();

		try {
			if (pluginFile.isFile()) {
				try (JarFile pluginJar = new JarFile(pluginFile, false)) {
					ZipEntry executionDescriptorEntry = pluginJar.getEntry(Properties.METAINF.RUNES4MAVEN.EXECUTIONS);

					if (executionDescriptorEntry != null) {
						InputStream is = pluginJar.getInputStream(executionDescriptorEntry);
						executionDescriptor = parseExecutionDescriptor(is, mvnPlugin, pluginFile.getAbsolutePath());
					}
				}
			} else {
				File executionXml = new File(pluginFile, Properties.METAINF.RUNES4MAVEN.EXECUTIONS);

				if (executionXml.isFile()) {
					try (InputStream is = new BufferedInputStream(new FileInputStream(executionXml))) {
						executionDescriptor = parseExecutionDescriptor(is, mvnPlugin, executionXml.getAbsolutePath());
					}
				}
			}
		} catch (IOException e) {
			throw new ExecutionDescriptorParsingException(mvnPlugin, pluginFile.getAbsolutePath(), e);
		}

		return executionDescriptor;
	}

	private ExecutionDescriptor parseExecutionDescriptor(InputStream is, org.apache.maven.model.Plugin mvnPlugin,
			String descriptorLocation) throws ExecutionDescriptorParsingException {
		try {
			Reader reader = ReaderFactory.newXmlReader(is);
			ExecutionDescriptor executionDescriptor = builder.newInstance(reader, descriptorLocation)
					.buildDescriptor();
			return executionDescriptor;
		} catch (IOException | XmlPullParserException e) {
			throw new ExecutionDescriptorParsingException(mvnPlugin, descriptorLocation, e);
		}
	}

}
