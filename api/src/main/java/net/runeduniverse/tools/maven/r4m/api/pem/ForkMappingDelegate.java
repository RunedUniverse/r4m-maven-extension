package net.runeduniverse.tools.maven.r4m.api.pem;

import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.api.pem.model.Fork;

public interface ForkMappingDelegate {

	public List<MojoExecution> calculateForkMappings(MojoExecution mojoExecution, final MavenSession mvnSession,
			final MavenProject mvnProject, final ExecutionArchiveSelectorConfig baseCnf, final Fork fork);

}
