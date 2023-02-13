package net.runeduniverse.tools.maven.r4m.pem.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelConfigParser;
import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelParser;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

@Component(role = ProjectExecutionModelConfigParser.class, hint = "default")
public class ConfigParser implements ProjectExecutionModelConfigParser {

	public static final String ERR_MSG_PARSE_PEM = "Failed to parse %s of maven project %s";

	@Requirement
	Logger log;

	@Requirement
	ProjectExecutionModelParser parser;

	@Override
	public Set<Execution> parse(MavenProject mvnProject) {

		File executionXml = new File(mvnProject.getBasedir(), Properties.PROJECT_EXECUTION_MODEL_FILE);

		ProjectExecutionModel model = null;
		if (executionXml.isFile()) {
			try (InputStream is = new BufferedInputStream(new FileInputStream(executionXml))) {
				model = this.parser.parseModel(is);
			} catch (IOException | XmlPullParserException e) {
				this.log.warn(
						String.format(ERR_MSG_PARSE_PEM, Properties.PROJECT_EXECUTION_MODEL_FILE, mvnProject.getName()),
						e);
			}
		}

		if (model == null)
			return new HashSet<>(0);

		return model.getExecutions();
	}

}
