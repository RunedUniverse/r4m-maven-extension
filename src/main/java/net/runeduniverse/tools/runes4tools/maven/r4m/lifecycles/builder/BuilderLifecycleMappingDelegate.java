package net.runeduniverse.tools.runes4tools.maven.r4m.lifecycles.builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecycleMappingDelegate;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.runes4tools.maven.r4m.Properties;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.ForkMojoDescriptor;
import net.runeduniverse.tools.runes4tools.maven.r4m.executions.AExecutionLifecycleMappingDelegate;
import net.runeduniverse.tools.runes4tools.maven.r4m.executions.DefaultExecutionArchiveParser;
import net.runeduniverse.tools.runes4tools.maven.r4m.executions.ExecutionBuilder;

@Component(role = LifecycleMappingDelegate.class, hint = Properties.LIFECYCLE.BUILDER.INVOKER.LIFECYCLE_INVOKER_HINT)
public class BuilderLifecycleMappingDelegate extends AExecutionLifecycleMappingDelegate {

	@Override
	public Map<String, List<MojoExecution>> calculateLifecycleMappings(MavenSession mvnSession, MavenProject mvnProject,
			Lifecycle lifecycle, String executingLifecyclePhase)
			throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
			MojoNotFoundException, InvalidPluginDescriptorException {

		Lifecycle targetLifecycle = this.lifecycles.get(Properties.LIFECYCLE.BUILDER.LIFECYCLE_HINT);
		DefaultExecutionArchiveParser parser = new DefaultExecutionArchiveParser(this.pluginManager, this.logger);

		for (Plugin mvnPlugin : mvnProject.getBuildPlugins()) {
			parser.parsePlugin(this.archive, mvnSession, mvnProject, mvnPlugin);
		}

		Plugin r4mPlugin = mvnProject.getPlugin(Properties.PLUGIN_KEY);

		String executionId;
		switch (executingLifecyclePhase) {
		case Properties.LIFECYCLE.BUILDER.INVOKER.PHASE_BUILD:
			executionId = Properties.LIFECYCLE.BUILDER.EXECUTION_DEFAULT_ID;
			break;

		case Properties.LIFECYCLE.BUILDER.INVOKER.PHASE_BUILD_TEST:
			executionId = Properties.LIFECYCLE.BUILDER.EXECUTION_TEST_ID;
			break;

		default:
			return Collections.emptyMap();
		}

		ExecutionBuilder builder = this.archive.createBuilder(mvnProject, executingLifecyclePhase, null,
				ForkMojoDescriptor.create(r4mPlugin, executingLifecyclePhase,
						Properties.LIFECYCLE.BUILDER.INVOKER.MOJO_SUMMARY, executionId));

		builder.applyLifecycle(targetLifecycle);

		builder.applyPluginFilter(filterForBuildPlugins(mvnProject));

		builder.applyExecutionId(executionId);

		return builder.build();
	}
}
