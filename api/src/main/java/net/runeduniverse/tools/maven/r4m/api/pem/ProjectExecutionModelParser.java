package net.runeduniverse.tools.maven.r4m.api.pem;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

public interface ProjectExecutionModelParser {
	ProjectExecutionModel parseModel(InputStream input) throws IOException, XmlPullParserException;
}
