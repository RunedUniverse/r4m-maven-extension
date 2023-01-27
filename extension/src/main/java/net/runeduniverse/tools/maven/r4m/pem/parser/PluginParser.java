package net.runeduniverse.tools.maven.r4m.pem.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.api.pem.PluginExecutionRegistry;
import net.runeduniverse.tools.maven.r4m.api.pem.PluginExecutionRegistrySlice;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelParser;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelPluginParser;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

@Component(role = ProjectExecutionModelPluginParser.class, hint = "default")
public class PluginParser implements ProjectExecutionModelPluginParser {

	public static final String ERR_MSG_PLUGIN_DESCRIPTOR = "Failed to acquire org.apache.maven.plugin.descriptor.PluginDescriptor!";
	public static final String ERR_MSG_PARSE_PEM = "Failed to parse pem of %s [%s]";

	@Requirement
	Logger log;

	@Requirement
	MavenPluginManager manager;

	@Requirement
	PluginExecutionRegistry registry;

	@Requirement
	ProjectExecutionModelParser parser;

	@Override
	public Set<Execution> parse(final List<RemoteRepository> repositories, final RepositorySystemSession session,
			Plugin mvnPlugin) {

		PluginExecutionRegistrySlice slice = this.registry.getSlice(mvnPlugin.getGroupId(), mvnPlugin.getArtifactId());
		if (slice == null)
			slice = parseSlice(repositories, session, mvnPlugin);
		if (slice == null)
			return new HashSet<>(0);

		return slice.getExecutions();
	}

	protected PluginExecutionRegistrySlice parseSlice(final List<RemoteRepository> repositories,
			final RepositorySystemSession session, Plugin mvnPlugin) {
		PluginDescriptor mvnPluginDescriptor = null;

		try {
			mvnPluginDescriptor = this.manager.getPluginDescriptor(mvnPlugin, repositories, session);
		} catch (PluginResolutionException | PluginDescriptorParsingException | InvalidPluginDescriptorException e) {
			this.log.error(ERR_MSG_PLUGIN_DESCRIPTOR, e);
			return null;
		}

		PluginExecutionRegistrySlice slice = this.registry.createSlice(mvnPluginDescriptor);
		ProjectExecutionModel model = null;

		File pluginFile = mvnPluginDescriptor.getPluginArtifact()
				.getFile();

		// TODO parse mvn plugin bound default executions in case config file does not
		// exist!!!

		try {
			if (pluginFile.isFile()) {
				try (JarFile pluginJar = new JarFile(pluginFile, false)) {
					ZipEntry executionDescriptorEntry = pluginJar.getEntry(Properties.METAINF.RUNES4MAVEN.EXECUTIONS);

					if (executionDescriptorEntry != null) {
						try (InputStream is = pluginJar.getInputStream(executionDescriptorEntry)) {
							model = this.parser.parseModel(is);
						}
					}
				}
			} else {
				File executionXml = new File(pluginFile, Properties.METAINF.RUNES4MAVEN.EXECUTIONS);

				if (executionXml.isFile()) {
					try (InputStream is = new BufferedInputStream(new FileInputStream(executionXml))) {
						model = this.parser.parseModel(is);
					}
				}
			}
		} catch (IOException | XmlPullParserException e) {
			this.log.error(String.format(ERR_MSG_PARSE_PEM, mvnPlugin.getKey(), pluginFile.getAbsolutePath()), e);
		}

		slice.includeModel(model);
		return slice;
	}

}
