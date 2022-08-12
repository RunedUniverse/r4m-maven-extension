package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.exec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecycleMappingDelegate;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.runes4tools.maven.runes4maven.Runes4MavenProperties;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.exec.internal.LifecycleDescriptor;

@Component(role = LifecycleMappingDelegate.class, hint = Runes4MavenProperties.LIFECYCLE.EXEC.LIFECYCLE_HINT)
public class Runes4MavenExecLifecycleMappingDelegate implements LifecycleMappingDelegate {

	@Requirement(role = Lifecycle.class)
	private Map<String, Lifecycle> lifecycles;

	@Requirement
	private BuildPluginManager pluginManager;

	@Override
	public Map<String, List<MojoExecution>> calculateLifecycleMappings(MavenSession session, MavenProject project,
			Lifecycle lifecycle, String lifecyclePhase) throws PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException {

		/*
		 * Initialize mapping from lifecycle phase to bound mojos. The key set of this
		 * map denotes the phases the caller is interested in, i.e. all phases of the
		 * described Lifecycle.
		 */

		Map<String, Map<Integer, List<MojoExecution>>> mappings = new LinkedHashMap<>();

		LifecycleDescriptor descriptor = LifecycleDescriptor.fromTask(lifecyclePhase);

		// example code - per phase & mojo
		MojoDescriptor mojoDescriptor = pluginManager.getMojoDescriptor(plugin, goal,
				project.getRemotePluginRepositories(), session.getRepositorySession());

		MojoExecution mojoExecution = new MojoExecution(mojoDescriptor, descriptor.getExecutionId(),
				MojoExecution.Source.LIFECYCLE);

		// TODO Auto-generated method stub

		Map<String, List<MojoExecution>> lifecycleMappings = new LinkedHashMap<>();

		for (Map.Entry<String, Map<Integer, List<MojoExecution>>> entry : mappings.entrySet()) {
			List<MojoExecution> mojoExecutions = new ArrayList<>();

			for (List<MojoExecution> executions : entry.getValue()
					.values()) {
				mojoExecutions.addAll(executions);
			}

			lifecycleMappings.put(entry.getKey(), mojoExecutions);
		}
		return lifecycleMappings;
	}

}
