package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem;

import java.util.List;
import java.util.Set;

import org.apache.maven.model.Plugin;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Execution;

public interface ProjectExecutionModelPluginParser {

	Set<Execution> parse(final List<RemoteRepository> repositories, final RepositorySystemSession session,
			Plugin mvnPlugin);

}
