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
package net.runeduniverse.tools.maven.r4m.grm.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.grm.converter.api.DataConverter;
import net.runeduniverse.tools.maven.r4m.grm.converter.api.ConfigurationFactory;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalContainer;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementModel;
import net.runeduniverse.tools.maven.r4m.grm.model.ModelProperties;
import net.runeduniverse.tools.maven.r4m.grm.writer.api.GoalRequirementModelWriter;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = GoalRequirementModelWriter.class, hint = "xml")
public class XmlWriter implements GoalRequirementModelWriter {

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
	public void writeModel(OutputStream stream, GoalRequirementModel grm) throws IOException {
		stream.write(convert(grm).toString()
				.getBytes());
	}

	@Override
	public void writeModel(OutputStream stream, GoalRequirementModel grm, Charset charset) throws IOException {
		stream.write(convert(grm).toString()
				.getBytes(charset));
	}

	@Override
	public void writeModel(OutputStream stream, GoalRequirementModel grm, String charsetName) throws IOException {
		stream.write(convert(grm).toString()
				.getBytes(charsetName));
	}

	protected PlexusConfiguration convert(final GoalRequirementModel grm) {
		final String version = getVersion(grm);
		final PlexusConfiguration node = this.factory.create("goal-requirement-model");
		node.setAttribute("xmlns", "https://api.runeduniverse.net/runes4tools/r4m-grm");
		node.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		node.setAttribute("xsi:schemaLocation",
				"https://api.runeduniverse.net/runes4tools/r4m-pem https://api.runeduniverse.net/runes4tools/r4m-grm-v"
						+ version.replace('.', '_') + ".xsd");
		node.addChild("modelVersion", version);

		final PlexusConfiguration goalsNode = node.getChild("goals", true);
		for (GoalContainer goal : grm.getGoalContainer()) {
			final PlexusConfiguration goalNode = convert(goal);
			if (goalNode != null)
				goalsNode.addChild(goalNode);
		}

		return node;
	}

	protected PlexusConfiguration convert(GoalContainer container) {
		return this.converter.convertContainer(this.factory, container);
	}

	protected String getVersion(final GoalRequirementModel grm) {
		final String version = grm.getVersion();
		if (isBlank(version))
			return ModelProperties.MODEL_VERSION;
		return version;
	}
}
