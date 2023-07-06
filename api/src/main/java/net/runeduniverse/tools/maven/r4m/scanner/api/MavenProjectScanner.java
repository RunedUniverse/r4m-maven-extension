package net.runeduniverse.tools.maven.r4m.scanner.api;

import java.util.Collection;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSlice;

/**
 * Required for scanning maven projects to extract pem configurations.
 */
public interface MavenProjectScanner extends Comparable<MavenProjectScanner> {

	@Override
	public default int compareTo(MavenProjectScanner o) {
		if (getPriority() == o.getPriority())
			return 0;
		if (getPriority() < o.getPriority())
			return -1;
		return 1;
	}

	public int getPriority();

	public void scan(final MavenSession mvnSession, final Collection<Plugin> extPlugins,
			final Set<Plugin> unidentifiablePlugins, final MavenProject mvnProject,
			final ExecutionArchiveSlice projectSlice) throws Exception;

}
