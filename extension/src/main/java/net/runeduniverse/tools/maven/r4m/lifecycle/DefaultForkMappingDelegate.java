package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.DefaultLifecycles;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecycleNotFoundException;
import org.apache.maven.lifecycle.MojoExecutionConfigurator;
import org.apache.maven.lifecycle.internal.MojoDescriptorCreator;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.lifecycle.Execution;
import org.apache.maven.plugin.lifecycle.Phase;
import org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import net.runeduniverse.tools.maven.r4m.api.lifecycle.AdvancedLifecycleMappingDelegate;
import net.runeduniverse.tools.maven.r4m.api.lifecycle.MojoExecutionData;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelector;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfigFactory;
import net.runeduniverse.tools.maven.r4m.api.pem.ForkMappingDelegate;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.api.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.api.pem.model.TargetPhase;
import net.runeduniverse.tools.maven.r4m.api.pem.view.ExecutionView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.LifecycleView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.PhaseView;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ForkMappingDelegate.class, hint = DefaultForkMappingDelegate.HINT, instantiationStrategy = "singleton")
public class DefaultForkMappingDelegate implements ForkMappingDelegate {

	public static final String HINT = "default";
	public static final String WARN_SKIPPING_UNKNOWN_LIFECYCLE = "skipping unknown lifecycle » %s";
	public static final String WARN_FAILED_TO_LOAD_PLUGIN_LIFECYCLE_CONFIGURATION = "Failed to load plugin-lifecycle specific configuration while forking » %s";

	@Requirement
	protected Logger log;
	@Requirement
	private MojoDescriptorCreator mojoDescriptorCreator;
	@Requirement(role = MojoExecutionConfigurator.class)
	private Map<String, MojoExecutionConfigurator> mojoExecutionConfigurators;
	@Requirement(role = Lifecycle.class)
	protected Map<String, Lifecycle> lifecycles;
	@Requirement
	protected ExecutionArchiveSelector selector;
	@Requirement
	private ExecutionArchiveSelectorConfigFactory cnfFactory;
	@Requirement
	private DefaultLifecycles defaultLifeCycles;
	@Requirement(hint = DefaultAdvancedLifecycleMappingDelegate.HINT)
	private AdvancedLifecycleMappingDelegate standardDelegate;
	@Requirement
	private Map<String, AdvancedLifecycleMappingDelegate> lifecycleMappingDelegates;

	protected List<String> calculateExecutingPhases(final ExecutionArchiveSelection selection, final Fork fork) {
		final Map<String, Set<String>> definedExecutionsPerPhase = new LinkedHashMap<>();
		final Map<String, Set<String>> excludedExecutionsPerPhase = new LinkedHashMap<>(0);
		// seed executing phases which might be defined
		List<String> phases = new LinkedList<>();
		if (fork.getPhases() != null)
			for (TargetPhase phase : fork.getPhases()) {
				phases.add(phase.getId());
				definedExecutionsPerPhase.put(phase.getId(), phase.getExecutions());
			}
		// seed phases if not set & existent from lifecycle
		TargetLifecycle targetLifecycle = fork.getLifecycle();
		if (targetLifecycle != null && phases.isEmpty()) {
			Lifecycle mvnLifecycle = this.lifecycles.get(targetLifecycle.getId());
			if (mvnLifecycle == null) {
				this.log.warn(String.format(WARN_SKIPPING_UNKNOWN_LIFECYCLE, targetLifecycle.getId()));
			} else {
				boolean includePhase = isBlank(targetLifecycle.getStartPhase());
				for (String phaseId : mvnLifecycle.getPhases()) {
					if (!includePhase && targetLifecycle.getStartPhase()
							.equals(phaseId))
						includePhase = true;
					if (includePhase) {
						phases.add(phaseId);
						definedExecutionsPerPhase.put(phaseId, new LinkedHashSet<>(0));
					}
					if (phaseId != null && phaseId.equals(targetLifecycle.getStopPhase()))
						break;
				}
			}
		}
		// seed excluded phases which might be defined
		for (TargetPhase phase : fork.getExcludedPhases())
			excludedExecutionsPerPhase.put(phase.getId(), phase.getExecutions());

		selection.modify(views -> {
			for (ExecutionView exec : views) {
				String executionId = exec.getId();
				for (LifecycleView lifecycle : exec.getLifecycles()
						.values())
					for (Iterator<PhaseView> iPhase = lifecycle.getPhases()
							.values()
							.iterator(); iPhase.hasNext();) {
						String phaseId = iPhase.next()
								.getId();
						Set<String> requiredExecutions = definedExecutionsPerPhase.get(phaseId);
						// if executions are explicitly defined, only run those
						if (requiredExecutions == null
								|| !requiredExecutions.isEmpty() && !requiredExecutions.contains(executionId)) {
							iPhase.remove();
							continue;
						}
						Set<String> excludedExecutions = excludedExecutionsPerPhase.get(phaseId);
						if (excludedExecutions != null) {
							if (excludedExecutions.isEmpty())
								iPhase.remove();
							else if (excludedExecutions.contains(executionId))
								iPhase.remove();
						}
					}
			}
		});
		return phases;
	}

	protected Map<String, List<MojoExecution>> calculateForkMappings(final MavenSession mvnSession,
			final MavenProject mvnProject, final Fork fork, ExecutionArchiveSelectorConfig cnf)
			throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
			MojoNotFoundException, InvalidPluginDescriptorException {

		final ExecutionArchiveSelection selection = this.selector.compileSelection(cnf);
		List<String> orderedPhases = calculateExecutingPhases(selection, fork);

		// Initialize mapping from lifecycle phase to bound mojos.
		Map<String, List<MojoExecution>> phaseToMojoMapping = new LinkedHashMap<>();
		for (String phase : orderedPhases) {

			Lifecycle lifecycle = defaultLifeCycles.get(phase);

			if (lifecycle == null) {
				this.log.error("[r4m] Unknown lifecycle phase \"" + phase + "\" detected when forking.");
				// throw new LifecyclePhaseNotFoundException("[r4m] Unknown lifecycle phase \""
				// + phase + "\" detected when forking.", phase);
				continue;
			}

			AdvancedLifecycleMappingDelegate delegate;
			if (Arrays.binarySearch(DefaultLifecycles.STANDARD_LIFECYCLES, lifecycle.getId()) >= 0) {
				delegate = standardDelegate;
			} else {
				delegate = lifecycleMappingDelegates.get(lifecycle.getId());
				if (delegate == null) {
					delegate = standardDelegate;
				}
			}

			for (Entry<String, List<MojoExecution>> item : delegate
					.calculateLifecycleMappings(mvnSession, mvnProject, lifecycle, phase, selection)
					.entrySet()) {
				List<MojoExecution> col = phaseToMojoMapping.get(item.getKey());
				if (col == null)
					phaseToMojoMapping.put(item.getKey(), item.getValue());
				else
					col.addAll(item.getValue());
			}

		}

		// reduce and order MojoExecution's
		Map<String, List<MojoExecution>> lifecycleMappings = new TreeMap<>();

		for (String phase : orderedPhases)
			lifecycleMappings.put(phase, phaseToMojoMapping.get(phase));

		return lifecycleMappings;
	}

	@Override
	public List<MojoExecution> calculateForkMappings(final MojoExecution mojoExecution, final MavenSession mvnSession,
			final MavenProject mvnProject) throws PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException {
		if (!(mojoExecution instanceof MojoExecutionData))
			return new LinkedList<>();

		MojoExecutionData mojoExecutionData = (MojoExecutionData) mojoExecution;
		ExecutionArchiveSelectorConfig baseCnf = mojoExecutionData.getExecutionArchiveSelectorConfig();
		ExecutionArchiveSelectorConfig cnf = this.cnfFactory.createEmptyConfig();
		Fork fork = mojoExecutionData.getFork();

		// select mvnProject
		cnf.selectActiveProject(mvnProject);
		// select packaging-procedure
		cnf.selectPackagingProcedure(baseCnf.getPackagingProcedure());
		// select modes
		if (isBlank(fork.getMode()))
			cnf.selectModes(baseCnf.getModes());
		else
			cnf.selectModes(fork.getMode());
		// select executions
		cnf.selectActiveExecutions(fork.getExecutions());

		Map<String, List<MojoExecution>> lifecycleMappings = calculateForkMappings(mvnSession, mvnProject, fork, cnf);

		// seed configuration with plugin-lifecycle specific settings
		// (defined by the plugin that defines the lifecycle/phase)
		try {
			injectLifecycleOverlay(lifecycleMappings, mvnSession, mvnProject, mojoExecution, fork.getLifecycleId());
		} catch (PluginNotFoundException | PluginDescriptorParsingException | LifecycleNotFoundException
				| MojoNotFoundException | PluginResolutionException | NoPluginFoundForPrefixException
				| InvalidPluginDescriptorException | PluginVersionResolutionException e) {

			String msg = String.format(WARN_FAILED_TO_LOAD_PLUGIN_LIFECYCLE_CONFIGURATION, mojoExecution.toString());
			if (this.log.isDebugEnabled())
				this.log.warn(msg, e);
			else
				this.log.warn(msg);
		}

		List<MojoExecution> mappings = new LinkedList<>();
		for (Entry<String, List<MojoExecution>> entry : lifecycleMappings.entrySet())
			if (entry.getValue() != null)
				for (MojoExecution exec : entry.getValue()) {
					selectExecutionConfigurator(exec.getMojoDescriptor()
							.getComponentConfigurator()).configure(mvnProject, mojoExecution, false);
					// complete with default values
					finalizeMojoConfiguration(mojoExecution);
					mappings.add(exec);
				}
		return mappings;
	}

	protected void injectLifecycleOverlay(final Map<String, List<MojoExecution>> lifecycleMappings,
			final MavenSession session, final MavenProject project, final MojoExecution mojoExecution,
			String forkedLifecycleId) throws PluginDescriptorParsingException, LifecycleNotFoundException,
			MojoNotFoundException, PluginNotFoundException, PluginResolutionException, NoPluginFoundForPrefixException,
			InvalidPluginDescriptorException, PluginVersionResolutionException {
		MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

		PluginDescriptor pluginDescriptor = mojoDescriptor.getPluginDescriptor();
		if (isBlank(forkedLifecycleId))
			forkedLifecycleId = mojoDescriptor.getExecuteLifecycle();

		if (StringUtils.isEmpty(forkedLifecycleId)) {
			return;
		}

		org.apache.maven.plugin.lifecycle.Lifecycle lifecycleOverlay;

		try {
			lifecycleOverlay = pluginDescriptor.getLifecycleMapping(forkedLifecycleId);
		} catch (IOException | XmlPullParserException e) {
			throw new PluginDescriptorParsingException(pluginDescriptor.getPlugin(), pluginDescriptor.getSource(), e);
		}

		if (lifecycleOverlay == null) {
			throw new LifecycleNotFoundException(forkedLifecycleId);
		}

		for (Phase phase : lifecycleOverlay.getPhases()) {
			List<MojoExecution> forkedExecutions = lifecycleMappings.get(phase.getId());

			if (forkedExecutions != null) {
				for (Execution execution : phase.getExecutions()) {
					for (String goal : execution.getGoals()) {
						MojoDescriptor forkedMojoDescriptor;

						if (goal.indexOf(':') < 0) {
							forkedMojoDescriptor = pluginDescriptor.getMojo(goal);
							if (forkedMojoDescriptor == null) {
								throw new MojoNotFoundException(goal, pluginDescriptor);
							}
						} else {
							forkedMojoDescriptor = this.mojoDescriptorCreator.getMojoDescriptor(goal, session, project);
						}

						MojoExecution forkedExecution = new MojoExecution(forkedMojoDescriptor,
								mojoExecution.getExecutionId());

						Xpp3Dom forkedConfiguration = (Xpp3Dom) execution.getConfiguration();

						forkedExecution.setConfiguration(forkedConfiguration);

						selectExecutionConfigurator(forkedExecution.getMojoDescriptor()
								.getComponentConfigurator()).configure(project, forkedExecution, true);

						forkedExecutions.add(forkedExecution);
					}
				}

				Xpp3Dom phaseConfiguration = (Xpp3Dom) phase.getConfiguration();

				if (phaseConfiguration != null) {
					for (MojoExecution forkedExecution : forkedExecutions) {
						Xpp3Dom forkedConfiguration = forkedExecution.getConfiguration();

						forkedConfiguration = Xpp3Dom.mergeXpp3Dom(phaseConfiguration, forkedConfiguration);

						forkedExecution.setConfiguration(forkedConfiguration);
					}
				}
			}
		}
	}

	protected MojoExecutionConfigurator selectExecutionConfigurator(String configuratorId) {
		if (configuratorId == null)
			configuratorId = "default";

		MojoExecutionConfigurator mojoExecutionConfigurator = mojoExecutionConfigurators.get(configuratorId);

		if (mojoExecutionConfigurator == null) {
			//
			// The plugin has a custom component configurator but does not have a custom
			// mojo execution configurator
			// so fall back to the default mojo execution configurator.
			//
			mojoExecutionConfigurator = mojoExecutionConfigurators.get("default");
		}
		return mojoExecutionConfigurator;
	}

	/**
	 * Post-processes the effective configuration for the specified mojo execution.
	 * This step discards all parameters from the configuration that are not
	 * applicable to the mojo and injects the default values for any missing
	 * parameters.
	 *
	 * @param mojoExecution The mojo execution whose configuration should be
	 *                      finalized, must not be {@code null}.
	 */
	protected void finalizeMojoConfiguration(final MojoExecution mojoExecution) {
		MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

		Xpp3Dom executionConfiguration = mojoExecution.getConfiguration();
		if (executionConfiguration == null) {
			executionConfiguration = new Xpp3Dom("configuration");
		}

		Xpp3Dom defaultConfiguration = MojoDescriptorCreator.convert(mojoDescriptor);

		Xpp3Dom finalConfiguration = new Xpp3Dom("configuration");

		if (mojoDescriptor.getParameters() != null) {
			for (Parameter parameter : mojoDescriptor.getParameters()) {
				Xpp3Dom parameterConfiguration = executionConfiguration.getChild(parameter.getName());

				if (parameterConfiguration == null) {
					parameterConfiguration = executionConfiguration.getChild(parameter.getAlias());
				}

				Xpp3Dom parameterDefaults = defaultConfiguration.getChild(parameter.getName());

				parameterConfiguration = Xpp3Dom.mergeXpp3Dom(parameterConfiguration, parameterDefaults, Boolean.TRUE);

				if (parameterConfiguration != null) {
					parameterConfiguration = new Xpp3Dom(parameterConfiguration, parameter.getName());

					if (StringUtils.isEmpty(parameterConfiguration.getAttribute("implementation"))
							&& StringUtils.isNotEmpty(parameter.getImplementation())) {
						parameterConfiguration.setAttribute("implementation", parameter.getImplementation());
					}

					finalConfiguration.addChild(parameterConfiguration);
				}
			}
		}

		mojoExecution.setConfiguration(finalConfiguration);
	}
}
