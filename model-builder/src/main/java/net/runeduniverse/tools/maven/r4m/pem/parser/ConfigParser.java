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
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

@Component(role = ProjectExecutionModelConfigParser.class, hint = ConfigParser.HINT)
public class ConfigParser implements ProjectExecutionModelConfigParser {

	public static final String HINT = "default";
	public static final String ERR_MSG_PARSE_PEM = "Failed to parse %s of maven project %s";

	@Requirement
	protected Logger log;
	@Requirement
	protected ProjectExecutionModelParser parser;

	@Override
	public ProjectExecutionModel parse(MavenProject mvnProject) throws Exception {

		File executionXml = new File(mvnProject.getBasedir(), Runes4MavenProperties.PROJECT_EXECUTION_MODEL_FILE);

		ProjectExecutionModel model = new ProjectExecutionModel(ConfigParser.class, ConfigParser.HINT);
		model.setEffective(true);

		if (executionXml.isFile()) {
			try (InputStream is = new BufferedInputStream(new FileInputStream(executionXml))) {
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
