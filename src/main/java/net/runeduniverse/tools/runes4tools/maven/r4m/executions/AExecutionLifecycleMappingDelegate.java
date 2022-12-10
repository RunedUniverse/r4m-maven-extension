package net.runeduniverse.tools.runes4tools.maven.r4m.executions;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecycleMappingDelegate;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.ExecutionArchive;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.filter.MvnPluginFilter;

public abstract class AExecutionLifecycleMappingDelegate implements LifecycleMappingDelegate {

	@Requirement(role = Lifecycle.class)
	protected Map<String, Lifecycle> lifecycles;

	@Requirement
	protected MavenPluginManager pluginManager;

	@Requirement
	protected Logger logger;

	// @Requirement
	protected ExecutionArchive archive;
	
	public AExecutionLifecycleMappingDelegate() {
		this.archive = new DefaultExecutionArchive();
	}
	
	protected static MvnPluginFilter filterForBuildPlugins(final MavenProject mvnProject) {
		final Set<org.apache.maven.model.Plugin> mvnPlugins = new LinkedHashSet<Plugin>(mvnProject.getBuildPlugins());
		return new MvnPluginFilter() {
			
			@Override
			public boolean apply(Plugin mvnPlugin) {
				return mvnPlugins.contains(mvnPlugin);
			}
		};
	}

}
