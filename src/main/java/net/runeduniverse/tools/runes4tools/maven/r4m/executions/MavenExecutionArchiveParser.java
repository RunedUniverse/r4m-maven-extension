package net.runeduniverse.tools.runes4tools.maven.r4m.executions;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.ExecutionArchive;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.ExecutionArchiveParser;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model.Plugin;

public class MavenExecutionArchiveParser implements ExecutionArchiveParser {

	public static final String ARCHIVE_PLUGIN_FLAG = "maven:config-parsed";

	@Override
	public void parsePlugin(final ExecutionArchive archive, MavenSession mvnSession, MavenProject mvnProject,
			org.apache.maven.model.Plugin mvnPlugin) {
		if (archive.isRegisteredWithFlag(mvnPlugin, ARCHIVE_PLUGIN_FLAG, true))
			return;

		Plugin plugin = null;
		if (archive.isRegistered(mvnPlugin))
			plugin = archive.getPlugin(mvnPlugin);
		else {
			plugin = new Plugin(mvnPlugin.getGroupId(), mvnPlugin.getArtifactId(), null);
			archive.register(mvnPlugin, plugin);
		}

		// TODO Extract DEFAULT calls from maven bindings

		/*
		 * Grab plugin executions that are bound to the selected lifecycle phases from
		 * project. The effective model of the project already contains the plugin
		 * executions induced by the project's packaging type. Remember, all phases of
		 * interest and only those are in the lifecycle mapping, if a phase has no value
		 * in the map, we are not interested in any of the executions bound to it.
		 */

		/*
		 * for (Plugin plugin : project.getBuild() .getPlugins()) { for (PluginExecution
		 * execution : plugin.getExecutions()) { // if the phase is specified then I
		 * don't have to go fetch the plugin yet and // pull it down // to examine the
		 * phase it is associated to. if (execution.getPhase() != null) { Map<Integer,
		 * List<MojoExecution>> phaseBindings = mappings.get(execution.getPhase()); if
		 * (phaseBindings != null) { for (String goal : execution.getGoals()) {
		 * MojoExecution mojoExecution = new MojoExecution(plugin, goal,
		 * execution.getId()); mojoExecution.setLifecyclePhase(execution.getPhase());
		 * addMojoExecution(phaseBindings, mojoExecution, execution.getPriority()); } }
		 * } // if not then i need to grab the mojo descriptor and look at the phase
		 * that is // specified else { for (String goal : execution.getGoals()) {
		 * MojoDescriptor mojoDescriptor = pluginManager.getMojoDescriptor(plugin, goal,
		 * project.getRemotePluginRepositories(), session.getRepositorySession());
		 * 
		 * Map<Integer, List<MojoExecution>> phaseBindings =
		 * mappings.get(mojoDescriptor.getPhase()); if (phaseBindings != null) {
		 * MojoExecution mojoExecution = new MojoExecution(mojoDescriptor,
		 * execution.getId());
		 * mojoExecution.setLifecyclePhase(mojoDescriptor.getPhase());
		 * addMojoExecution(phaseBindings, mojoExecution, execution.getPriority()); } }
		 * } } }
		 */

		archive.setFlag(mvnPlugin, ARCHIVE_PLUGIN_FLAG, true);
	}

}
