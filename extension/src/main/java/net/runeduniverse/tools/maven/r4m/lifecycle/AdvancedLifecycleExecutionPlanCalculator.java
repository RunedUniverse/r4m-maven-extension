package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.DefaultLifecycles;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecycleNotFoundException;
import org.apache.maven.lifecycle.LifecyclePhaseNotFoundException;
import org.apache.maven.lifecycle.MavenExecutionPlan;
import org.apache.maven.lifecycle.MojoExecutionConfigurator;
import org.apache.maven.lifecycle.internal.DefaultMojoExecutionConfigurator;
import org.apache.maven.lifecycle.internal.ExecutionPlanItem;
import org.apache.maven.lifecycle.internal.GoalTask;
import org.apache.maven.lifecycle.internal.LifecycleDependencyResolver;
import org.apache.maven.lifecycle.internal.LifecycleExecutionPlanCalculator;
import org.apache.maven.lifecycle.internal.LifecyclePluginResolver;
import org.apache.maven.lifecycle.internal.LifecycleTask;
import org.apache.maven.lifecycle.internal.MojoDescriptorCreator;
import org.apache.maven.lifecycle.internal.builder.BuilderCommon;
import org.apache.maven.plugin.BuildPluginManager;
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
import org.apache.maven.plugin.version.PluginVersionResolver;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import net.runeduniverse.tools.maven.r4m.api.lifecycle.AdvancedLifecycleMappingDelegate;
import net.runeduniverse.tools.maven.r4m.api.lifecycle.LifecycleTaskData;
import net.runeduniverse.tools.maven.r4m.api.lifecycle.LifecycleTaskParser;
import net.runeduniverse.tools.maven.r4m.api.lifecycle.MojoExecutionData;
import net.runeduniverse.tools.maven.r4m.api.lifecycle.PhaseSequenceCalculatorDelegate;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelector;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelectorConfigFactory;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = LifecycleExecutionPlanCalculator.class)
public class AdvancedLifecycleExecutionPlanCalculator implements LifecycleExecutionPlanCalculator {

	@Requirement
	private PluginVersionResolver pluginVersionResolver;

	@Requirement
	private BuildPluginManager pluginManager;

	@Requirement
	private DefaultLifecycles defaultLifeCycles;

	@Requirement
	private MojoDescriptorCreator mojoDescriptorCreator;

	@Requirement
	private ExecutionArchiveSelectorConfigFactory selectorConfigFactory;

	@Requirement
	private LifecyclePluginResolver lifecyclePluginResolver;

	@Requirement
	private LifecycleTaskParser lifecycleTaskParser;

	@Requirement
	protected ExecutionArchiveSelector selector;

	@Requirement
	private Map<String, PhaseSequenceCalculatorDelegate> phaseSequenceDelegates;

	@Requirement(hint = DefaultAdvancedLifecycleMappingDelegate.HINT)
	private AdvancedLifecycleMappingDelegate standardDelegate;

	@Requirement
	private Map<String, AdvancedLifecycleMappingDelegate> lifecycleMappingDelegates;

	@Requirement
	private Map<String, MojoExecutionConfigurator> mojoExecutionConfigurators;

	public AdvancedLifecycleExecutionPlanCalculator() {
	}

	// Only used for testing
	public AdvancedLifecycleExecutionPlanCalculator(BuildPluginManager pluginManager,
			DefaultLifecycles defaultLifeCycles, MojoDescriptorCreator mojoDescriptorCreator,
			LifecyclePluginResolver lifecyclePluginResolver) {
		this.pluginManager = pluginManager;
		this.defaultLifeCycles = defaultLifeCycles;
		this.mojoDescriptorCreator = mojoDescriptorCreator;
		this.lifecyclePluginResolver = lifecyclePluginResolver;
		this.mojoExecutionConfigurators = Collections.singletonMap("default",
				(MojoExecutionConfigurator) new DefaultMojoExecutionConfigurator());
	}

	@Override
	public MavenExecutionPlan calculateExecutionPlan(MavenSession session, MavenProject project, List<Object> tasks,
			boolean setup) throws PluginNotFoundException, PluginResolutionException, LifecyclePhaseNotFoundException,
			PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException,
			NoPluginFoundForPrefixException, LifecycleNotFoundException, PluginVersionResolutionException {
		lifecyclePluginResolver.resolveMissingPluginVersions(project, session);

		final List<MojoExecution> executions = calculateMojoExecutions(session, project, tasks);

		if (setup) {
			setupMojoExecutions(session, project, executions);
		}

		final List<ExecutionPlanItem> planItem = ExecutionPlanItem.createExecutionPlanItems(project, executions);

		return new MavenExecutionPlan(planItem, defaultLifeCycles);
	}

	@Override
	public MavenExecutionPlan calculateExecutionPlan(MavenSession session, MavenProject project, List<Object> tasks)
			throws PluginNotFoundException, PluginResolutionException, LifecyclePhaseNotFoundException,
			PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException,
			NoPluginFoundForPrefixException, LifecycleNotFoundException, PluginVersionResolutionException {
		return calculateExecutionPlan(session, project, tasks, true);
	}

	private void setupMojoExecutions(MavenSession session, MavenProject project, List<MojoExecution> mojoExecutions)
			throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
			MojoNotFoundException, InvalidPluginDescriptorException, NoPluginFoundForPrefixException,
			LifecyclePhaseNotFoundException, LifecycleNotFoundException, PluginVersionResolutionException {
		for (MojoExecution mojoExecution : mojoExecutions) {
			setupMojoExecution(session, project, mojoExecution);
		}
	}

	@Override
	public void setupMojoExecution(MavenSession session, MavenProject project, MojoExecution mojoExecution)
			throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
			MojoNotFoundException, InvalidPluginDescriptorException, NoPluginFoundForPrefixException,
			LifecyclePhaseNotFoundException, LifecycleNotFoundException, PluginVersionResolutionException {
		MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

		if (mojoDescriptor == null) {
			mojoDescriptor = pluginManager.getMojoDescriptor(mojoExecution.getPlugin(), mojoExecution.getGoal(),
					project.getRemotePluginRepositories(), session.getRepositorySession());

			mojoExecution.setMojoDescriptor(mojoDescriptor);
		}

		selectExecutionConfigurator(mojoExecution).configure(project, mojoExecution,
				MojoExecution.Source.CLI.equals(mojoExecution.getSource()));

		finalizeMojoConfiguration(mojoExecution);

		calculateForkedExecutions(mojoExecution, session, project, new HashSet<MojoDescriptor>());
	}

	public List<MojoExecution> calculateMojoExecutions(MavenSession session, MavenProject project, List<Object> tasks)
			throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
			MojoNotFoundException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
			PluginVersionResolutionException, LifecyclePhaseNotFoundException {
		final List<MojoExecution> mojoExecutions = new ArrayList<>();

		for (Object task : tasks) {
			ExecutionArchiveSelectorConfig selectorConfig = this.selectorConfigFactory.createEmptyConfig()
					.selectActiveProject(project)
					.selectPackagingProcedure(project.getPackaging());

			if (task instanceof GoalTask) {
				String pluginGoal = ((GoalTask) task).toString();

				String executionId = "default-cli";
				int executionIdx = pluginGoal.indexOf('@');
				if (executionIdx > 0) {
					executionId = pluginGoal.substring(executionIdx + 1);
				}

				MojoDescriptor mojoDescriptor = mojoDescriptorCreator.getMojoDescriptor(pluginGoal, session, project);

				MojoExecutionAdapter mojoExecution = new MojoExecutionAdapter(mojoDescriptor, executionId,
						MojoExecution.Source.CLI, selectorConfig.selectModes("default")
								.selectActiveExecutions(executionId));

				mojoExecutions.add(mojoExecution);
			} else if (task instanceof LifecycleTask) {
				LifecycleTaskData taskData = this.lifecycleTaskParser.parse((LifecycleTask) task);
				String mode = taskData.getMode();

				Map<String, List<MojoExecution>> phaseToMojoMapping = calculateLifecycleMappings(session, project,
						taskData.getLifecyclePhase(),
						this.selector.compileSelection(selectorConfig.selectModes(isBlank(mode) ? "default" : mode)
								.selectActiveExecutions(taskData.getExecution())));

				for (List<MojoExecution> mojoExecutionsFromLifecycle : phaseToMojoMapping.values()) {
					mojoExecutions.addAll(mojoExecutionsFromLifecycle);
				}
			} else {
				throw new IllegalStateException("unexpected task " + task);
			}
		}
		return mojoExecutions;
	}

	protected Map<String, List<MojoExecution>> calculateLifecycleMappings(final MavenSession mvnSession,
			final MavenProject mvnProject, String lifecyclePhase, final ExecutionArchiveSelection selection)
			throws LifecyclePhaseNotFoundException, PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException {
		/*
		 * Determine the lifecycle that corresponds to the given phase.
		 */

		boolean isFlagged = lifecyclePhase.charAt(0) == '+';
		if (isFlagged)
			lifecyclePhase = lifecyclePhase.substring(1);

		Lifecycle lifecycle = defaultLifeCycles.get(lifecyclePhase);

		if (lifecycle == null) {
			throw new LifecyclePhaseNotFoundException("[r4m] Unknown lifecycle phase \"" + lifecyclePhase
					+ "\". You must specify a valid lifecycle phase in the format [<mode>/][+]<phase>[@<execution>]"
					+ " or a goal in the format <plugin-prefix>:<goal>[@<execution>] or"
					+ " <plugin-group-id>:<plugin-artifact-id>[:<plugin-version>]:<goal>[@<execution>]. Available lifecycle phases are: "
					+ defaultLifeCycles.getLifecyclePhaseList() + ".", lifecyclePhase);
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

		// TODO define selectedPhaseSeqCalc via property
		String selectedPhaseSeqCalc = "declared";
		if (isFlagged)
			if (selectedPhaseSeqCalc == DeclaredSeqCalculatorDelegate.HINT)
				selectedPhaseSeqCalc = SequentialSeqCalculatorDelegate.HINT;
			else
				selectedPhaseSeqCalc = DeclaredSeqCalculatorDelegate.HINT;

		PhaseSequenceCalculatorDelegate phaseSeqCalcDelegate = this.phaseSequenceDelegates.get(selectedPhaseSeqCalc);
		if (phaseSeqCalcDelegate == null)
			throw new LifecyclePhaseNotFoundException(
					"[r4m] PhaseSequenceCalculatorDelegate<" + selectedPhaseSeqCalc + "> not found!", lifecyclePhase);

		Map<String, List<MojoExecution>> phaseToMojoMapping = new LinkedHashMap<>();

		for (String phase : phaseSeqCalcDelegate.calculatePhaseSequence(lifecycle, lifecyclePhase))
			for (Entry<String, List<MojoExecution>> item : delegate
					.calculateLifecycleMappings(mvnSession, mvnProject, lifecycle, phase, selection)
					.entrySet()) {
				List<MojoExecution> col = phaseToMojoMapping.get(item.getKey());
				if (col == null)
					phaseToMojoMapping.put(item.getKey(), item.getValue());
				else
					col.addAll(item.getValue());
			}

		return phaseToMojoMapping;
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
	private void finalizeMojoConfiguration(MojoExecution mojoExecution) {
		MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

		Xpp3Dom executionConfiguration = mojoExecution.getConfiguration();
		if (executionConfiguration == null) {
			executionConfiguration = new Xpp3Dom("configuration");
		}

		Xpp3Dom defaultConfiguration = getMojoConfiguration(mojoDescriptor);

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

	private Xpp3Dom getMojoConfiguration(MojoDescriptor mojoDescriptor) {
		return MojoDescriptorCreator.convert(mojoDescriptor);
	}

	@Override
	public void calculateForkedExecutions(MojoExecution mojoExecution, MavenSession session)
			throws MojoNotFoundException, PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
			LifecyclePhaseNotFoundException, LifecycleNotFoundException, PluginVersionResolutionException {
		calculateForkedExecutions(mojoExecution, session, session.getCurrentProject(), new HashSet<MojoDescriptor>());
	}

	private void calculateForkedExecutions(MojoExecution mojoExecution, MavenSession session, MavenProject project,
			Collection<MojoDescriptor> alreadyForkedExecutions)
			throws MojoNotFoundException, PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
			LifecyclePhaseNotFoundException, LifecycleNotFoundException, PluginVersionResolutionException {
		MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

		if (!mojoDescriptor.isForking()) {
			return;
		}

		if (!alreadyForkedExecutions.add(mojoDescriptor)) {
			return;
		}

		List<MavenProject> forkedProjects = LifecycleDependencyResolver.getProjects(project, session,
				mojoDescriptor.isAggregator());

		for (MavenProject forkedProject : forkedProjects) {
			if (forkedProject != project) {
				lifecyclePluginResolver.resolveMissingPluginVersions(forkedProject, session);
			}

			List<MojoExecution> forkedExecutions;

			// TODO INJECT HERE
			if (StringUtils.isEmpty(mojoDescriptor.getExecutePhase())) {
				forkedExecutions = calculateForkedGoal(mojoExecution, session, forkedProject, alreadyForkedExecutions);
			} else {
				forkedExecutions = calculateForkedLifecycle(mojoExecution, session, forkedProject,
						alreadyForkedExecutions);
			}

			mojoExecution.setForkedExecutions(BuilderCommon.getKey(forkedProject), forkedExecutions);
		}

		alreadyForkedExecutions.remove(mojoDescriptor);
	}

	private List<MojoExecution> calculateForkedLifecycle(MojoExecution mojoExecution, MavenSession session,
			MavenProject project, Collection<MojoDescriptor> alreadyForkedExecutions)
			throws MojoNotFoundException, PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
			LifecyclePhaseNotFoundException, LifecycleNotFoundException, PluginVersionResolutionException {
		MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

		String forkedPhase = mojoDescriptor.getExecutePhase();

		// TODO integrate DefaultForkMappingDelegate
		Map<String, List<MojoExecution>> lifecycleMappings = calculateLifecycleMappings(session, project, forkedPhase,
				null);

		for (List<MojoExecution> forkedExecutions : lifecycleMappings.values()) {
			for (MojoExecution forkedExecution : forkedExecutions) {
				if (forkedExecution.getMojoDescriptor() == null) {
					MojoDescriptor forkedMojoDescriptor = pluginManager.getMojoDescriptor(forkedExecution.getPlugin(),
							forkedExecution.getGoal(), project.getRemotePluginRepositories(),
							session.getRepositorySession());

					forkedExecution.setMojoDescriptor(forkedMojoDescriptor);
				}

				selectExecutionConfigurator(forkedExecution).configure(project, forkedExecution, false);
			}
		}

		//injectLifecycleOverlay(lifecycleMappings, mojoExecution, session, project);

		List<MojoExecution> mojoExecutions = new ArrayList<>();

		for (List<MojoExecution> forkedExecutions : lifecycleMappings.values()) {
			for (MojoExecution forkedExecution : forkedExecutions) {
				if (!alreadyForkedExecutions.contains(forkedExecution.getMojoDescriptor())) {
					finalizeMojoConfiguration(forkedExecution);

					calculateForkedExecutions(forkedExecution, session, project, alreadyForkedExecutions);

					mojoExecutions.add(forkedExecution);
				}
			}
		}

		return mojoExecutions;
	}



	// org.apache.maven.plugins:maven-remote-resources-plugin:1.0:process
	// take repo mans into account as one may be aggregating prefixes of many
	// collect at the root of the repository, read the one at the root, and
	// fetch remote if something is missing
	// or the user forces the issue

	private List<MojoExecution> calculateForkedGoal(MojoExecution mojoExecution, MavenSession session,
			MavenProject project, Collection<MojoDescriptor> alreadyForkedExecutions)
			throws MojoNotFoundException, PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
			LifecyclePhaseNotFoundException, LifecycleNotFoundException, PluginVersionResolutionException {
		MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

		PluginDescriptor pluginDescriptor = mojoDescriptor.getPluginDescriptor();

		String forkedGoal = mojoDescriptor.getExecuteGoal();

		MojoDescriptor forkedMojoDescriptor = pluginDescriptor.getMojo(forkedGoal);
		if (forkedMojoDescriptor == null) {
			throw new MojoNotFoundException(forkedGoal, pluginDescriptor);
		}

		if (alreadyForkedExecutions.contains(forkedMojoDescriptor)) {
			return Collections.emptyList();
		}

		MojoExecutionAdapter forkedExecution;
		if (mojoExecution instanceof MojoExecutionData)
			forkedExecution = new MojoExecutionAdapter(forkedMojoDescriptor, forkedGoal,
					((MojoExecutionData) mojoExecution).getExecutionArchiveSelectorConfig());
		else
			forkedExecution = new MojoExecutionAdapter(forkedMojoDescriptor, forkedGoal,
					this.selectorConfigFactory.createEmptyConfig()
							.selectActiveProject(project)
							.selectModes("default")
							.selectPackagingProcedure(project.getPackaging())
							.selectActiveExecutions(mojoExecution.getExecutionId()));

		selectExecutionConfigurator(forkedExecution).configure(project, forkedExecution, true);

		finalizeMojoConfiguration(forkedExecution);

		calculateForkedExecutions(forkedExecution, session, project, alreadyForkedExecutions);

		return Collections.singletonList(forkedExecution);
	}

	private MojoExecutionConfigurator selectExecutionConfigurator(MojoExecution mojoExecution) {
		String configuratorId = mojoExecution.getMojoDescriptor()
				.getComponentConfigurator();
		if (configuratorId == null) {
			configuratorId = "default";
		}

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
}
