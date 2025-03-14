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
package net.runeduniverse.tools.maven.r4m.grm.parser;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.io.InputStream;
import java.io.Reader;
import org.apache.commons.io.input.XmlStreamReader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

import net.runeduniverse.tools.maven.r4m.grm.converter.api.DataConverter;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementSource;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementModel;
import net.runeduniverse.tools.maven.r4m.grm.parser.api.GoalRequirementModelParser;

@Component(role = GoalRequirementModelParser.class, hint = "xml")
public class XmlParser implements GoalRequirementModelParser {

	@Requirement
	protected DataConverter converter;

	@Override
	public void parseModel(final GoalRequirementModel grm, final InputStream input) throws Exception {
		final Reader reader = new XmlStreamReader(input);
		final PlexusConfiguration cnf = new XmlPlexusConfiguration(Xpp3DomBuilder.build(reader));

		parseModelVersion(grm, cnf.getChild("modelVersion", false));
		parseGoals(grm, cnf.getChild("goals", false), grm.getDefaultGroupId(), grm.getDefaultArtifactId(),
				grm.getDefaultSource());
	}

	protected boolean parseModelVersion(final GoalRequirementModel model, final PlexusConfiguration versionNode) {
		if (versionNode == null)
			return false;
		final String value = versionNode.getValue();
		if (isBlank(value))
			return false;
		model.setVersion(value.trim());
		return true;
	}

	protected boolean parseGoals(final GoalRequirementModel model, final PlexusConfiguration nodeList,
			final String defaultGroupId, final String defaultArtifactId, final GoalRequirementSource defaultSource) {
		if (nodeList == null || nodeList.getChildCount() == 0)
			return false;

		final PlexusConfiguration nodes[] = nodeList.getChildren("goal");
		for (int i = 0; i < nodes.length; i++) {
			model.addGoalContainer(
					converter.convertContainer(nodes[i], defaultGroupId, defaultArtifactId, defaultSource));
		}
		return true;
	}
}
