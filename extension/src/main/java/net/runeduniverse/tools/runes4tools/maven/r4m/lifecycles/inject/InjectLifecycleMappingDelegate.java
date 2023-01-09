package net.runeduniverse.tools.runes4tools.maven.r4m.lifecycles.inject;

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

import net.runeduniverse.tools.runes4tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.ExecutionBuilder;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.ForkMojoDescriptor;
import net.runeduniverse.tools.runes4tools.maven.r4m.executions.AExecutionLifecycleMappingDelegate;
import net.runeduniverse.tools.runes4tools.maven.r4m.executions.ConfigExecutionArchiveParser;

@Component(role = LifecycleMappingDelegate.class, hint = Runes4MavenProperties.LIFECYCLE.INJECT.LIFECYCLE_HINT)
public class InjectLifecycleMappingDelegate extends AExecutionLifecycleMappingDelegate {

	@Override
	public Map<String, List<MojoExecution>> calculateLifecycleMappings(MavenSession mvnSession, MavenProject mvnProject,
			Lifecycle injectLifecycle, String injectLifecyclePhase)
			throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
			MojoNotFoundException, InvalidPluginDescriptorException {

		LifecycleDescriptor targetLifecycleDescriptor = LifecycleDescriptor.fromTask(injectLifecyclePhase);
		Lifecycle targetLifecycle = this.lifecycles.get(targetLifecycleDescriptor.getLifecycleId());
		ConfigExecutionArchiveParser parser = new ConfigExecutionArchiveParser(this.pluginManager, this.logger);

		for (Plugin mvnPlugin : mvnProject.getBuildPlugins()) {
			parser.parsePlugin(this.archive, mvnSession, mvnProject, mvnPlugin);
		}

		// TODO add correct data
		Plugin thisPlugin = mvnProject.getPlugin("net.runeduniverse.tools.runes4tools:runes4maven-maven-plugin");
		ExecutionBuilder builder = this.archive.createBuilder(mvnProject, "builder", null,
				ForkMojoDescriptor.create(thisPlugin, "scan-references", "builder-scan-references", null));

		builder.applyLifecycle(targetLifecycle);

		builder.applyPluginFilter(filterForBuildPlugins(mvnProject));

		builder.applyExecutionId(targetLifecycleDescriptor.getExecutionId());

		return builder.build();
	}
}
