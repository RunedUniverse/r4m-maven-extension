/*
 * Copyright © 2024 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.tools.maven.r4m.grm.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.grm.api.PluginRequirementRegistry;
import net.runeduniverse.tools.maven.r4m.grm.api.PluginRequirementRegistrySector;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementSource;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementModel;
import net.runeduniverse.tools.maven.r4m.grm.parser.api.GoalRequirementModelParser;
import net.runeduniverse.tools.maven.r4m.grm.parser.api.GoalRequirementModelPluginParser;

@Component(role = GoalRequirementModelPluginParser.class, hint = PluginParser.HINT)
public class PluginParser implements GoalRequirementModelPluginParser {

	public static final String HINT = "default";
	public static final String ERR_MSG_PLUGIN_DESCRIPTOR = "Failed to acquire org.apache.maven.plugin.descriptor.PluginDescriptor!";
	public static final String ERR_MSG_PARSE_GRM = "Failed to parse grm of %s [%s]";

	@Requirement
	protected Logger log;
	@Requirement
	protected MavenPluginManager manager;
	@Requirement
	protected PluginRequirementRegistry registry;
	@Requirement(hint = "xml")
	protected GoalRequirementModelParser parser;

	@Override
	public GoalRequirementModel parse(final List<RemoteRepository> repositories, final RepositorySystemSession session,
			Plugin mvnPlugin) throws Exception {

		PluginDescriptor mvnPluginDescriptor = null;

		try {
			mvnPluginDescriptor = this.manager.getPluginDescriptor(mvnPlugin, repositories, session);
		} catch (PluginDescriptorParsingException | InvalidPluginDescriptorException e) {
			this.log.error(ERR_MSG_PLUGIN_DESCRIPTOR, e);
			return null;
		}

		PluginRequirementRegistrySector sector = this.registry.getSector(mvnPlugin.getGroupId(),
				mvnPlugin.getArtifactId());
		if (sector == null)
			sector = this.registry.createSector(mvnPluginDescriptor);

		GoalRequirementModel model = sector.getModel(PluginParser.class, PluginParser.HINT);
		if (model != null)
			return model;

		model = parseModel(mvnPluginDescriptor);
		sector.includeModel(model);
		return model;
	}

	protected GoalRequirementModel parseModel(final PluginDescriptor mvnPluginDescriptor) throws Exception {
		final GoalRequirementModel model = new GoalRequirementModel();
		final File pluginFile = mvnPluginDescriptor.getPluginArtifact()
				.getFile();

		model.setParser(PluginParser.class, PluginParser.HINT);
		model.setDefaultGroupId(mvnPluginDescriptor.getGroupId());
		model.setDefaultArtifactId(mvnPluginDescriptor.getArtifactId());
		model.setDefaultSource(GoalRequirementSource.PLUGIN);

		try {
			if (pluginFile.isFile()) {
				try (JarFile pluginJar = new JarFile(pluginFile, false)) {
					final ZipEntry xmlFileEntry = pluginJar
							.getEntry(Runes4MavenProperties.METAINF.RUNES4MAVEN.GOAL_REQUIREMENT_MODEL_FILE);

					if (xmlFileEntry != null) {
						try (InputStream is = pluginJar.getInputStream(xmlFileEntry)) {
							this.parser.parseModel(model, is);
						}
					}
				}
			} else {
				final File xmlFile = new File(pluginFile,
						Runes4MavenProperties.METAINF.RUNES4MAVEN.GOAL_REQUIREMENT_MODEL_FILE);

				if (xmlFile.isFile()) {
					try (InputStream is = new BufferedInputStream(new FileInputStream(xmlFile))) {
						this.parser.parseModel(model, is);
					}
				}
			}
		} catch (IOException | XmlPullParserException e) {
			this.log.error(String.format(ERR_MSG_PARSE_GRM, mvnPluginDescriptor.getPlugin()
					.getKey(), pluginFile.getAbsolutePath()));
			throw e;
		}
		return model;
	}
}
