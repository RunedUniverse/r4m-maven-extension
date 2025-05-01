/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.pem.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelWriter;
import net.runeduniverse.tools.maven.r4m.pem.converter.api.ConfigurationFactory;
import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataConverter;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelProperties;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ProjectExecutionModelWriter.class, hint = "xml", instantiationStrategy = "singleton")
public class XmlWriter implements ProjectExecutionModelWriter {

	protected final ConfigurationFactory<PlexusConfiguration> factory;

	@Requirement
	protected DataConverter converter;

	public XmlWriter() {
		this(XmlPlexusConfiguration::new);
	}

	public XmlWriter(final ConfigurationFactory<PlexusConfiguration> factory) {
		this.factory = factory;
	}

	@Override
	public void writeModel(final OutputStream stream, final ProjectExecutionModel pem) throws IOException {
		stream.write(convert(pem).toString()
				.getBytes());
	}

	@Override
	public void writeModel(final OutputStream stream, final ProjectExecutionModel pem, final Charset charset)
			throws IOException {
		stream.write(convert(pem).toString()
				.getBytes(charset));
	}

	@Override
	public void writeModel(final OutputStream stream, final ProjectExecutionModel pem, final String charsetName)
			throws IOException {
		stream.write(convert(pem).toString()
				.getBytes(charsetName));
	}

	@Override
	public PlexusConfiguration convert(final ProjectExecutionModel pem) {
		final String version = getVersion(pem);
		final PlexusConfiguration node = new XmlPlexusConfiguration("project-execution-model");
		node.setAttribute("xmlns", "https://api.runeduniverse.net/runes4tools/r4m-pem");
		node.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		node.setAttribute("xsi:schemaLocation",
				"https://api.runeduniverse.net/runes4tools/r4m-pem https://api.runeduniverse.net/runes4tools/r4m-pem-v"
						+ version.replace('.', '_') + ".xsd");
		if (pem.isEffective())
			node.setAttribute("super-pem", "true");
		node.addChild("modelVersion", version);

		final PlexusConfiguration executionsNode = node.getChild("executions", true);
		for (Execution exec : pem.getExecutions()) {
			final PlexusConfiguration execNode = this.converter.convertEntry(this.factory, exec);
			if (node == null)
				continue;
			executionsNode.addChild(execNode);
		}

		return node;
	}

	protected String getVersion(final ProjectExecutionModel pem) {
		final String version = pem.getVersion();
		if (isBlank(version))
			return ModelProperties.MODEL_VERSION;
		return version;
	}
}
