package net.runeduniverse.tools.maven.r4m.api.pem;

import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.project.MavenProject;

public interface ForkMappingDelegate {

	public List<MojoExecution> calculateForkMappings(MojoExecution mojoExecution, final MavenSession mvnSession,
			final MavenProject mvnProject) throws PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException;

}
