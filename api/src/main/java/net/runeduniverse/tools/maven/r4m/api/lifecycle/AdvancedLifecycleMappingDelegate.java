package net.runeduniverse.tools.maven.r4m.api.lifecycle;

import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;

/**
 * Lifecycle mapping delegate component interface. Calculates project build
 * execution plan given {@link Lifecycle} and lifecycle phase. Standard
 * lifecycles use plugin execution {@code <phase>} or mojo default lifecycle
 * phase to calculate the execution plan, but custom lifecycles can use
 * alternative mapping strategies.
 * <p>
 * Implementations of this interface must be annotated with either
 * {@code @Named("lifecycle-id")} or equivalent plexus {@code @Component}
 * annotations.
 *
 * @since 1.0.0
 * @author VenaNocta
 */
public interface AdvancedLifecycleMappingDelegate {

	public Map<String, List<MojoExecution>> calculateLifecycleMappings(final MavenSession mvnSession,
			final MavenProject mvnProject, final Lifecycle lifecycle, final String lifecyclePhase,
			final ExecutionArchiveSelection selection) throws PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException;

}
