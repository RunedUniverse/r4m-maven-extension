package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.MojoExecutionConfigurator;
import org.apache.maven.lifecycle.internal.MojoDescriptorCreator;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecution.Source;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelector;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfigFactory;
import net.runeduniverse.tools.maven.r4m.api.pem.ForkMappingDelegate;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.api.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.api.pem.model.TargetPhase;
import net.runeduniverse.tools.maven.r4m.api.pem.view.ExecutionView;
import net.runeduniverse.tools.maven.r4m.api.pem.view.GoalView;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = ForkMappingDelegate.class, hint = DefaultForkMappingDelegate.HINT, instantiationStrategy = "singleton")
public class DefaultForkMappingDelegate implements ForkMappingDelegate {

	public static final String HINT = "default";
	public static final String WARN_SKIPPING_UNKNOWN_LIFECYCLE = "skipping unknown lifecycle » %s";

	@Requirement
	protected Logger log;
	@Requirement(role = MojoExecutionConfigurator.class)
	private Map<String, MojoExecutionConfigurator> mojoExecutionConfigurators;
	@Requirement(role = Lifecycle.class)
	protected Map<String, Lifecycle> lifecycles;
	@Requirement
	protected ExecutionArchiveSelector selector;
	@Requirement
	private ExecutionArchiveSelectorConfigFactory cnfFactory;

	protected List<String> calculateExecutingPhases(final Map<String, Set<String>> executionsPerPhase,
			final Fork fork) {
		// seed executing phases which might be defined
		List<String> phases = new LinkedList<>();
		if (fork.getPhases() != null)
			for (TargetPhase phase : fork.getPhases()) {
				phases.add(phase.getId());
				executionsPerPhase.put(phase.getId(), phase.getExecutions());
			}
		// seed phases if not set & existent from lifecycle
		TargetLifecycle lifecycle = fork.getLifecycle();
		if (lifecycle != null && phases.isEmpty()) {
			Lifecycle mvnLifecycle = this.lifecycles.get(lifecycle.getId());
			if (mvnLifecycle == null) {
				this.log.warn(String.format(WARN_SKIPPING_UNKNOWN_LIFECYCLE, lifecycle.getId()));
				return phases;
			}
			
			boolean includePhase = isBlank(lifecycle.getStartPhase());
			for (String phaseId : mvnLifecycle.getPhases()) {
				if (!includePhase && lifecycle.getStartPhase()
						.equals(phaseId))
					includePhase = true;
				if (includePhase) {
					phases.add(phaseId);
					executionsPerPhase.put(phaseId, new LinkedHashSet<>(0));
				}
				if (phaseId != null && phaseId.equals(lifecycle.getStopPhase()))
					break;
			}
		}
		return phases;
	}

	protected Map<String, Set<String>> calculateExcludedPhases(final Fork fork) {
		// seed excluded phases which might be defined
		Map<String, Set<String>> excludePhases = new LinkedHashMap<>(0);
		for (TargetPhase phase : fork.getExcludedPhases())
			excludePhases.put(phase.getId(), phase.getExecutions());
		return excludePhases;
	}

	protected List<MojoExecution> calculateForkMappings(final MavenProject mvnProject, final Fork fork,
			final ExecutionArchiveSelection selection) {

		Map<String, Set<String>> executionsPerPhase = new LinkedHashMap<>();
		List<String> orderedPhases = calculateExecutingPhases(executionsPerPhase, fork);
		Map<String, Set<String>> excludePhases = calculateExcludedPhases(fork);

		// Initialize mapping from lifecycle phase to bound mojos.
		Map<String, Map<Integer, List<MojoExecution>>> mappings = new LinkedHashMap<>();
		for (String phase : orderedPhases) {
			mappings.put(phase, new TreeMap<>());
			Set<String> requiredExecutions = executionsPerPhase.get(phase);
			for (Entry<ExecutionView, List<GoalView>> entry : selection.selectPhase(phase)
					.entrySet()) {
				String executionId = entry.getKey()
						.getId();
				// if executions are explicitly defined, only run those
				if (!requiredExecutions.isEmpty() && !requiredExecutions.contains(executionId))
					continue;
				if (excludePhases.containsKey(executionId)) {
					Set<String> excludedExecutions = excludePhases.get(executionId);
					if (excludedExecutions.isEmpty())
						continue;
					else if (excludedExecutions.contains(executionId))
						continue;
				}

				for (GoalView goal : entry.getValue()) {
					Map<Integer, List<MojoExecution>> phaseBindings = mappings.get(phase);
					if (phaseBindings != null) {
						MojoExecution mojoExecution = new MojoExecution(goal.getDescriptor(), executionId,
								Source.LIFECYCLE);
						mojoExecution.setLifecyclePhase(phase);
						addMojoExecution(phaseBindings, mojoExecution, 0);
						// call goal bound MojoExecutionConfigurator
						selectExecutionConfigurator(goal.getDescriptor()
								.getComponentConfigurator()).configure(mvnProject, mojoExecution, false);
						// complete with default values
						finalizeMojoConfiguration(mojoExecution);
					}
				}
			}
		}

		// reduce and order MojoExecution's
		List<MojoExecution> lifecycleMappings = new LinkedList<>();

		for (String phase : orderedPhases)
			for (List<MojoExecution> entryExecutions : mappings.get(phase)
					.values())
				lifecycleMappings.addAll(entryExecutions);

		return lifecycleMappings;
	}

	@Override
	public List<MojoExecution> calculateForkMappings(final MavenSession mvnSession, final MavenProject mvnProject,
			final ExecutionArchiveSelectorConfig baseCnf, final Fork fork) {

		ExecutionArchiveSelectorConfig cnf = this.cnfFactory.createEmptyConfig();
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

		return calculateForkMappings(mvnProject, fork, this.selector.compileSelection(cnf));
	}

	protected void addMojoExecution(final Map<Integer, List<MojoExecution>> phaseBindings,
			final MojoExecution mojoExecution, int priority) {
		List<MojoExecution> mojoExecutions = phaseBindings.get(priority);

		if (mojoExecutions == null) {
			mojoExecutions = new ArrayList<>();
			phaseBindings.put(priority, mojoExecutions);
		}

		mojoExecutions.add(mojoExecution);
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
