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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelConfigParser;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelParser;
import net.runeduniverse.tools.maven.r4m.pem.model.DefaultModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

@Component(role = ProjectExecutionModelConfigParser.class, hint = ConfigParser.HINT)
public class ConfigParser implements ProjectExecutionModelConfigParser {

	public static final String HINT = "default";
	public static final String ERR_MSG_PARSE_PEM = "Failed to parse %s of maven project %s";

	@Requirement
	protected Logger log;
	@Requirement(hint = "xml")
	protected ProjectExecutionModelParser parser;

	@Override
	public ProjectExecutionModel parse(final MavenProject mvnProject) throws Exception {
		final File xmlFile = new File(mvnProject.getBasedir(), Runes4MavenProperties.PROJECT_EXECUTION_MODEL_FILE);
		final ProjectExecutionModel model = new ProjectExecutionModel();
		model.setModelSource(new DefaultModelSource() //
				.setProjectId(ModelSource.id(mvnProject::getGroupId, mvnProject::getArtifactId))
				.setFile(xmlFile.toPath()));
		model.setUserDefined(true);
		model.setParser(ConfigParser.class, ConfigParser.HINT);

		if (xmlFile.isFile()) {
			try (InputStream is = new BufferedInputStream(new FileInputStream(xmlFile))) {
				this.parser.parseModel(model, is);
			} catch (IOException | XmlPullParserException e) {
				this.log.error(String.format(ERR_MSG_PARSE_PEM, Runes4MavenProperties.PROJECT_EXECUTION_MODEL_FILE,
						mvnProject.getName()));
				throw e;
			}
		}

		return model;
	}

}
