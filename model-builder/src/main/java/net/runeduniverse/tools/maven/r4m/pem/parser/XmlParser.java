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
package net.runeduniverse.tools.maven.r4m.pem.parser;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.io.InputStream;
import java.io.Reader;
import org.apache.commons.io.input.XmlStreamReader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelParser;
import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataConverter;
import net.runeduniverse.tools.maven.r4m.pem.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

@Component(role = ProjectExecutionModelParser.class, hint = "xml")
public class XmlParser implements ProjectExecutionModelParser {

	@Requirement
	protected DataConverter converter;

	@Override
	public void parseModel(final ProjectExecutionModel pem, final InputStream input) throws Exception {
		final Reader reader = new XmlStreamReader(input);
		final PlexusConfiguration cnf = new XmlPlexusConfiguration(Xpp3DomBuilder.build(reader));

		parseModelVersion(pem, cnf.getChild("modelVersion", false));
		parseExecutions(pem, cnf.getChild("executions", false));
	}

	protected boolean parseModelVersion(final ProjectExecutionModel model, final PlexusConfiguration versionNode) {
		if (versionNode == null)
			return false;
		final String value = versionNode.getValue();
		if (isBlank(value))
			return false;
		model.setVersion(value);
		return true;
	}

	protected boolean parseExecutions(final ProjectExecutionModel model, final PlexusConfiguration nodeList) {
		if (nodeList == null || nodeList.getChildCount() == 0)
			return false;

		final PlexusConfiguration execNodes[] = nodeList.getChildren("execution");
		if (execNodes.length > 0) {
			for (PlexusConfiguration execNode : execNodes) {
				final DataEntry entry = this.converter.convertEntry(null, execNode);
				if (entry instanceof Execution)
					model.addExecution((Execution) entry);
			}
		}
		return true;
	}
}
