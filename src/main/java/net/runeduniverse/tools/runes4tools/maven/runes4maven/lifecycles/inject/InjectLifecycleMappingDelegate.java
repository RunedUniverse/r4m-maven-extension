package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecycleMappingDelegate;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.Runes4MavenProperties;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.DefaultExecutionArchiveParser;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.ExecutionArchive;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.LifecycleDescriptor;

@Component(role = LifecycleMappingDelegate.class, hint = Runes4MavenProperties.LIFECYCLE.INJECT.LIFECYCLE_HINT)
public class InjectLifecycleMappingDelegate implements LifecycleMappingDelegate {

	@Requirement(role = Lifecycle.class)
	private Map<String, Lifecycle> lifecycles;

	@Requirement
	private MavenPluginManager pluginManager;

	private ExecutionArchive archive;

	@Override
	public Map<String, List<MojoExecution>> calculateLifecycleMappings(MavenSession session, MavenProject project,
			Lifecycle execLifecycle, String execLifecyclePhase)
			throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
			MojoNotFoundException, InvalidPluginDescriptorException {

		/*
		 * Initialize mapping from lifecycle phase to bound mojos. The key set of this
		 * map denotes the phases the caller is interested in, i.e. all phases of the
		 * described Lifecycle.
		 */

		Map<String, Map<Integer, List<MojoExecution>>> mappings = new LinkedHashMap<>();

		LifecycleDescriptor targetLifecycleDescriptor = LifecycleDescriptor.fromTask(execLifecyclePhase);
		Lifecycle targetLifecycle = this.lifecycles.get(targetLifecycleDescriptor.getLifecycleId());
		DefaultExecutionArchiveParser parser = new DefaultExecutionArchiveParser(this.pluginManager);

		for (Plugin mvnPlugin : project.getBuild()
				.getPlugins()) {
			parser.parsePlugin(this.archive, mvnPlugin);
		}

		// seed mappings with defined lifecycle phases
		for (String phase : targetLifecycle.getPhases())
			mappings.put(phase, new TreeMap<>());

		XmlStreamReader inputStreamReader;

		Xpp3Dom executionCfg = Xpp3DomBuilder.build(inputStreamReader, "UTF-8");
		// Xpp3Dom executionCfg = Xpp3DomBuilder.build(inputStreamReader, false);

		project.getBuild()
				.getPlugins()
				.get(0)
				.getExecutions()
				.get(0);



		// for Goal
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
