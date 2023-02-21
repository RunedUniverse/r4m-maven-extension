package net.runeduniverse.tools.maven.r4m.pem.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
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
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

@Component(role = ProjectExecutionModelPluginParser.class, hint = PluginParser.HINT)
public class PluginParser implements ProjectExecutionModelPluginParser {

	public static final String HINT = "default";
	public static final String ERR_MSG_PLUGIN_DESCRIPTOR = "Failed to acquire org.apache.maven.plugin.descriptor.PluginDescriptor!";
	public static final String ERR_MSG_PARSE_PEM = "Failed to parse pem of %s [%s]";

	@Requirement
	protected Logger log;
	@Requirement
	protected MavenPluginManager manager;
	@Requirement
	protected PluginExecutionRegistry registry;
	@Requirement
	protected ProjectExecutionModelParser parser;

	@Override
	public ProjectExecutionModel parse(final List<RemoteRepository> repositories, final RepositorySystemSession session,
			Plugin mvnPlugin) throws Exception {

		PluginExecutionRegistrySlice slice = this.registry.getSlice(mvnPlugin.getGroupId(), mvnPlugin.getArtifactId());
		ProjectExecutionModel model = new ProjectExecutionModel(HINT);
		if (slice == null)
			slice = parseSlice(repositories, session, mvnPlugin, model);

		return model;
	}

	protected PluginExecutionRegistrySlice parseSlice(final List<RemoteRepository> repositories,
			final RepositorySystemSession session, Plugin mvnPlugin, ProjectExecutionModel model) throws Exception {
		PluginDescriptor mvnPluginDescriptor = null;

		try {
			mvnPluginDescriptor = this.manager.getPluginDescriptor(mvnPlugin, repositories, session);
		} catch (PluginResolutionException | PluginDescriptorParsingException | InvalidPluginDescriptorException e) {
			this.log.error(ERR_MSG_PLUGIN_DESCRIPTOR, e);
			return null;
		}

		PluginExecutionRegistrySlice slice = this.registry.createSlice(mvnPluginDescriptor);

		File pluginFile = mvnPluginDescriptor.getPluginArtifact()
				.getFile();
		boolean pemMissing = true;

		try {
			if (pluginFile.isFile()) {
				try (JarFile pluginJar = new JarFile(pluginFile, false)) {
					ZipEntry executionDescriptorEntry = pluginJar.getEntry(Properties.METAINF.RUNES4MAVEN.EXECUTIONS);

					if (executionDescriptorEntry != null) {
						try (InputStream is = pluginJar.getInputStream(executionDescriptorEntry)) {
							this.parser.parseModel(model, is);
							pemMissing = false;
						}
					}
				}
			} else {
				File executionXml = new File(pluginFile, Properties.METAINF.RUNES4MAVEN.EXECUTIONS);

				if (executionXml.isFile()) {
					try (InputStream is = new BufferedInputStream(new FileInputStream(executionXml))) {
						this.parser.parseModel(model, is);
						pemMissing = false;
					}
				}
			}
		} catch (IOException | XmlPullParserException e) {
			this.log.error(String.format(ERR_MSG_PARSE_PEM, mvnPlugin.getKey(), pluginFile.getAbsolutePath()));
			throw e;
		}

		// TODO parse mvn plugin bound default executions in case config file does not
		// exist!!!
		if (pemMissing)
			for (PluginExecution execution : mvnPlugin.getExecutions()) {
				// if the phase is specified then I don't have to
				// go fetch the plugin yet and
				// pull it down
				// to examine the phase it is associated to.
				/*
				 * if (execution.getPhase() != null) { Map<Integer, List<MojoExecution>>
				 * phaseBindings = mappings.get(execution.getPhase()); if (phaseBindings !=
				 * null) { for (String goal : execution.getGoals()) { MojoExecution
				 * mojoExecution = new MojoExecution(mvnPlugin, goal, execution.getId());
				 * mojoExecution.setLifecyclePhase(execution.getPhase());
				 * addMojoExecution(phaseBindings, mojoExecution, execution.getPriority()); } }
				 * }
				 */
				// if not then i need to grab the mojo descriptor and look at the phase that is
				// specified
				/*
				 * else { for (String goal : execution.getGoals()) { MojoDescriptor
				 * mojoDescriptor = pluginManager.getMojoDescriptor(mvnPlugin, goal,
				 * mvnProject.getRemotePluginRepositories(), session.getRepositorySession());
				 * 
				 * Map<Integer, List<MojoExecution>> phaseBindings =
				 * mappings.get(mojoDescriptor.getPhase()); if (phaseBindings != null) {
				 * MojoExecution mojoExecution = new MojoExecution(mojoDescriptor,
				 * execution.getId());
				 * mojoExecution.setLifecyclePhase(mojoDescriptor.getPhase());
				 * addMojoExecution(phaseBindings, mojoExecution, execution.getPriority()); } }
				 * }
				 */
			}

		slice.includeModel(model);
		return slice;
	}

}
