package net.runeduniverse.tools.maven.r4m.api.pem;

import java.util.List;
import org.apache.maven.model.Plugin;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import net.runeduniverse.tools.maven.r4m.api.pem.model.ProjectExecutionModel;

public interface ProjectExecutionModelPluginParser {

	public ProjectExecutionModel parse(final List<RemoteRepository> repositories, final RepositorySystemSession session,
			final Plugin mvnPlugin) throws Exception;

}
