package net.runeduniverse.tools.runes4tools.maven.runes4maven.executions;

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

import net.runeduniverse.tools.runes4tools.maven.runes4maven.Properties;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.ExecutionArchive;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.ExecutionArchiveParser;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.model.Plugin;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.errors.ExecutionDescriptorParsingException;

public class DefaultExecutionArchiveParser implements ExecutionArchiveParser {

	// @Requirement
	private MavenPluginManager pluginManager;

	// @Requirement
	private Logger logger;

	private ExecutionDescriptorBuilder builder = new ExecutionDescriptorBuilder();

	public DefaultExecutionArchiveParser(MavenPluginManager pluginManager, Logger logger) {
		this.pluginManager = pluginManager;
		this.logger = logger;
	}

	public void parsePlugin(final ExecutionArchive archive, final MavenSession mvnSession,
			final MavenProject mvnProject, org.apache.maven.model.Plugin mvnPlugin) {
		if (archive.contains(mvnPlugin))
			return;
		// parse Maven-Plugin -> executions.xml
		Plugin plugin = new Plugin(mvnPlugin.getGroupId(), mvnPlugin.getArtifactId(), null);
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
