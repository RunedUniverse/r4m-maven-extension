package net.runeduniverse.tools.maven.r4m.pem.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import net.runeduniverse.tools.maven.r4m.api.pem.ProjectExecutionModelParser;
import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

@Component(role = ProjectExecutionModelParser.class, hint = "default")
public class PEMParser implements ProjectExecutionModelParser {

	@Override
	public ProjectExecutionModel parseModel(InputStream input) throws IOException, XmlPullParserException {
		Reader reader = ReaderFactory.newXmlReader(input);

		PlexusConfiguration cnf = new XmlPlexusConfiguration(Xpp3DomBuilder.build(reader));

		// TODO Auto-generated method stub
		return null;
	}

}
