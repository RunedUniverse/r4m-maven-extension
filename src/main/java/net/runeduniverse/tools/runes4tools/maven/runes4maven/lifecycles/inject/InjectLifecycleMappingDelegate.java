package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecycleMappingDelegate;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.Runes4MavenProperties;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.DefaultExecutionArchiveParser;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.ExecutionArchive;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.ExecutionBuilder;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.LifecycleDescriptor;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.inject.internal.filter.MvnPluginFilter;

@Component(role = LifecycleMappingDelegate.class, hint = Runes4MavenProperties.LIFECYCLE.INJECT.LIFECYCLE_HINT)
public class InjectLifecycleMappingDelegate implements LifecycleMappingDelegate {

	@Requirement(role = Lifecycle.class)
	private Map<String, Lifecycle> lifecycles;

	@Requirement
	private MavenPluginManager pluginManager;

	// @Requirement
	private ExecutionArchive archive;

	public InjectLifecycleMappingDelegate() {
		this.archive = new ExecutionArchive();
	}

	@Override
	public Map<String, List<MojoExecution>> calculateLifecycleMappings(MavenSession session, MavenProject mvnProject,
			Lifecycle injectLifecycle, String injectLifecyclePhase)
			throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
			MojoNotFoundException, InvalidPluginDescriptorException {

		LifecycleDescriptor targetLifecycleDescriptor = LifecycleDescriptor.fromTask(injectLifecyclePhase);
		Lifecycle targetLifecycle = this.lifecycles.get(targetLifecycleDescriptor.getLifecycleId());
		DefaultExecutionArchiveParser parser = new DefaultExecutionArchiveParser(this.pluginManager);

		for (Plugin mvnPlugin : mvnProject.getBuild()
				.getPlugins()) {
			parser.parsePlugin(this.archive, mvnPlugin);
		}

		ExecutionBuilder builder = this.archive.createBuilder(mvnProject);

		builder.applyLifecycle(targetLifecycle);

		builder.applyPluginFilter(new MvnBuildPluginFilter(mvnProject));

		builder.applyExecutionId(targetLifecycleDescriptor.getExecutionId());

		return builder.build();
	}

	private class MvnBuildPluginFilter implements MvnPluginFilter {

		private final Set<org.apache.maven.model.Plugin> mvnPlugins;

		protected MvnBuildPluginFilter(MavenProject mvnProject) {
			this.mvnPlugins = new LinkedHashSet<Plugin>(mvnProject.getBuildPlugins());
		}

		@Override
		public boolean apply(Plugin plugin) {
			return this.mvnPlugins.contains(plugin);
		}

	}
}
