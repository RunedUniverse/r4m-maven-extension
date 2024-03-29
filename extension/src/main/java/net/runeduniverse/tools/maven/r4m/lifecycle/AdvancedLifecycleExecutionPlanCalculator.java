/*
 * Copyright © 2024 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
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
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.api.Settings;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.AdvancedLifecycleMappingDelegate;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.GoalTaskData;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskData;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskRequest;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskRequestCalculatorDelegate;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.MojoExecutionData;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.TaskData;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.TaskParser;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelector;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfigFactory;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetLifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.TargetPhase;
import net.runeduniverse.tools.maven.r4m.pem.view.api.ExecutionView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.LifecycleView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.PhaseView;

@Component(role = LifecycleExecutionPlanCalculator.class, instantiationStrategy = "singleton")
public class AdvancedLifecycleExecutionPlanCalculator implements LifecycleExecutionPlanCalculator {

	public static final String WARN_SKIPPING_UNKNOWN_LIFECYCLE = //
			"[%s] skipping unknown lifecycle » %s";
	public static final String ERR_UNKNOWN_LIFECYCLE_PHASE_ON_FORK = //
			"[%s] Unknown lifecycle phase \"%s\" detected when forking";

	@Requirement
	protected Logger log;

	@Requirement
	protected Settings settings;

	@Requirement
	private BuildPluginManager pluginManager;

	@Requirement
	private MojoDescriptorCreator mojoDescriptorCreator;

	@Requirement
	private ExecutionArchiveSelectorConfigFactory selectorConfigFactory;

	@Requirement
	private DefaultLifecycles defaultLifeCycles;

	@Requirement(role = Lifecycle.class)
	protected Map<String, Lifecycle> lifecycles;

	@Requirement
	private LifecyclePluginResolver lifecyclePluginResolver;

	@Requirement(role = TaskParser.class)
	private Map<String, TaskParser> taskParser;

	@Requirement
	protected ExecutionArchiveSelector selector;

	@Requirement(role = LifecycleTaskRequestCalculatorDelegate.class)
	private Map<String, LifecycleTaskRequestCalculatorDelegate> lifecycleTaskRequestDelegates;

	@Requirement(hint = DefaultAdvancedLifecycleMappingDelegate.HINT)
	private AdvancedLifecycleMappingDelegate standardDelegate;

	@Requirement(role = AdvancedLifecycleMappingDelegate.class)
	private Map<String, AdvancedLifecycleMappingDelegate> lifecycleMappingDelegates;

	@Requirement(role = MojoExecutionConfigurator.class)
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

		final List<MojoExecution> executions = calculateMojoExecutions(session, project, resolveTasks(tasks));

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

	protected void setupMojoExecutions(MavenSession session, MavenProject project, List<MojoExecution> mojoExecutions)
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

		// using LinkedList for Forks since it calls equals() on contains
		calculateForks(session, session.getCurrentProject(), mojoExecution, new LinkedList<>(), new LinkedHashSet<>());
	}

	protected List<TaskData> resolveTasks(final List<Object> tasks) {
		final List<TaskData> resolvedTasks = new LinkedList<>();
		for (Object task : tasks)
			if (task instanceof GoalTask) {
				resolvedTasks.add(this.taskParser.get("goal")
						.parse(task));
			} else if (task instanceof LifecycleTask) {
				resolvedTasks.add(this.taskParser.get("lifecycle")
						.parse(task));
			} else {
				TaskData data = null;
				for (TaskParser parser : this.taskParser.values()) {
					data = parser.parse(task);
					if (data != null)
						break;
				}
				if (data == null)
					throw new IllegalStateException("unexpected task " + task);
				resolvedTasks.add(data);
			}
		return resolvedTasks;
	}

	public List<MojoExecution> calculateMojoExecutions(MavenSession session, MavenProject project, List<TaskData> tasks)
			throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException,
			MojoNotFoundException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
			PluginVersionResolutionException, LifecyclePhaseNotFoundException {
		final List<MojoExecution> mojoExecutions = new ArrayList<>();

		ExecutionArchiveSelectorConfig selectorConfig = this.selectorConfigFactory.createEmptyConfig()
				.selectActiveProject(project)
				.selectAllActiveProfiles(project.getActiveProfiles())
				.selectPackagingProcedure(project.getPackaging());

		if ("top-level".equals(this.settings.getActiveProfilesInheritance()
				.getSelected())) {
			selectorConfig.selectAllActiveProfiles(session.getTopLevelProject()
					.getActiveProfiles());
		}
		if ("upstream".equals(this.settings.getActiveProfilesInheritance()
				.getSelected())) {
			for (MavenProject parent = project.getParent(); parent != null; parent = parent.getParent())
				selectorConfig.selectAllActiveProfiles(parent.getActiveProfiles());
		}

		final LifecycleTaskRequestCalculatorDelegate lifecycleTaskReqCalcDelegate = selectLifecycleTaskReqCalcDelegate(
				this.settings.getLifecycleTaskRequestCalculator()
						.getSelected());

		for (TaskData task : tasks)
			if (task instanceof GoalTaskData) {
				GoalTaskData data = (GoalTaskData) task;

				MojoDescriptor mojoDescriptor = this.mojoDescriptorCreator.getMojoDescriptor(data.getGoalTask(),
						session, project);

				MojoExecutionAdapter mojoExecution = new MojoExecutionAdapter(mojoDescriptor, data.getExecutions()[0],
						MojoExecution.Source.CLI, selectorConfig.clone()
								.selectModes("default")
								.selectActiveExecutions(data.getPrimaryExecutionOrDefault("default-cli")));

				mojoExecutions.add(mojoExecution);
			} else if (task instanceof LifecycleTaskData) {
				LifecycleTaskData taskData = (LifecycleTaskData) task;

				ExecutionArchiveSelectorConfig taskSelectorConfig = selectorConfig.clone();
				if (taskData.hasValidModes())
					taskSelectorConfig.selectModes(taskData.getModes());
				else
					taskSelectorConfig.selectModes("default");

				final ExecutionArchiveSelection selection = this.selector
						.compileSelection(taskSelectorConfig.selectActiveExecutions(taskData.getExecutions()));
				for (LifecycleTaskRequest request : lifecycleTaskReqCalcDelegate.calculateTaskRequest(taskData)) {
					Map<String, List<MojoExecution>> phaseToMojoMapping = calculateLifecycleMappings(session, project,
							request, selection);
					for (List<MojoExecution> mojoExecutionsFromLifecycle : phaseToMojoMapping.values())
						for (MojoExecution exec : mojoExecutionsFromLifecycle) {
							if (this.settings.getGeneratePluginExecutions()
									.getSelected())
								generatePluginExecutions(project, exec);
							mojoExecutions.add(exec);
						}
				}
			}
		return mojoExecutions;
	}

	protected Map<String, List<MojoExecution>> calculateLifecycleMappings(final MavenSession mvnSession,
			final MavenProject mvnProject, LifecycleTaskRequest taskRequest, final ExecutionArchiveSelection selection)
			throws LifecyclePhaseNotFoundException, PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException {
		/*
		 * Determine the lifecycle that corresponds to the given phase.
		 */

		Lifecycle lifecycle = taskRequest.getLifecycle();
		AdvancedLifecycleMappingDelegate delegate;
		if (Arrays.binarySearch(DefaultLifecycles.STANDARD_LIFECYCLES, lifecycle.getId()) >= 0) {
			delegate = standardDelegate;
		} else {
			delegate = lifecycleMappingDelegates.get(lifecycle.getId());
			if (delegate == null) {
				delegate = standardDelegate;
			}
		}

		Map<String, List<MojoExecution>> phaseToMojoMapping = new LinkedHashMap<>();

		for (String phase : taskRequest.getPhaseSequence())
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

	protected void generatePluginExecutions(final MavenProject mvnProject, final MojoExecution mojoExecution) {
		Build build = mvnProject.getBuild();
		String groupId = mojoExecution.getGroupId();
		String artifactId = mojoExecution.getArtifactId();
		String goal = mojoExecution.getGoal();
		String executionId = mojoExecution.getExecutionId();

		Plugin plugin = findPlugin(groupId, artifactId, build.getPlugins());
		Plugin managedPlugin = findPlugin(groupId, artifactId, build.getPluginManagement()
				.getPlugins());

		Object cnfObj = null;
		if (plugin != null)
			cnfObj = plugin.getConfiguration();
		if (isBlank(cnfObj) && managedPlugin != null)
			cnfObj = managedPlugin.getConfiguration();
		if (isBlank(cnfObj))
			return;

		PluginExecution exec = null;
		if (plugin != null) {
			// local plugin
			Xpp3Dom currentConfig = null;
			for (PluginExecution e : plugin.getExecutions())
				if (executionId.equals(e.getId())) {
					exec = e;
					if (exec.getConfiguration() instanceof Xpp3Dom)
						currentConfig = (Xpp3Dom) exec.getConfiguration();
					break;
				}
			if (exec == null && managedPlugin != null)
				for (PluginExecution e : managedPlugin.getExecutions())
					if (executionId.equals(e.getId())) {
						exec = e.clone();
						exec.getGoals()
								.clear();
						exec.setInherited(false);
						plugin.addExecution(exec);
						break;
					}
			if (exec == null) {
				exec = new PluginExecution();
				exec.setId(executionId);
				exec.setInherited(false);
				plugin.addExecution(exec);
			} else
				exec.removeGoal(goal);
			exec.addGoal(goal);
			if (currentConfig != null && cnfObj instanceof Xpp3Dom)
				exec.setConfiguration(Xpp3Dom.mergeXpp3Dom(currentConfig, (Xpp3Dom) cnfObj, true));
			else
				exec.setConfiguration(cnfObj);
		} else if (managedPlugin != null) {
			// managed plugin
			plugin = managedPlugin.clone();
			plugin.setConfiguration(null);
			plugin.setInherited(false);
			for (PluginExecution e : managedPlugin.getExecutions())
				if (executionId.equals(e.getId())) {
					exec = e.clone();
					exec.getGoals()
							.clear();
				}
			if (exec == null) {
				exec = new PluginExecution();
				exec.setId(executionId);
			}
			exec.setInherited(false);
			exec.addGoal(goal);
			exec.setConfiguration(cnfObj);
			plugin.addExecution(exec);
			build.addPlugin(plugin);
			build.flushPluginMap();
		}
	}

	protected boolean isBlank(final Object configuration) {
		if (configuration == null)
			return true;
		if (configuration instanceof Xpp3Dom) {
			Xpp3Dom dom = (Xpp3Dom) configuration;
			return isBlank(dom.getValue()) && dom.getChildCount() == 0;
		}
		return false;
	}

	protected Plugin findPlugin(final String groupId, final String artifactId, final Collection<Plugin> plugins) {
		for (Plugin plugin : plugins)
			if (groupId.equals(plugin.getGroupId()) && artifactId.equals(plugin.getArtifactId()))
				return plugin;
		return null;
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

	@Override
	public void calculateForkedExecutions(MojoExecution mojoExecution, MavenSession session)
			throws MojoNotFoundException, PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
			LifecyclePhaseNotFoundException, LifecycleNotFoundException, PluginVersionResolutionException {
		// using LinkedList for Forks since it calls equals() on contains
		calculateForks(session, session.getCurrentProject(), mojoExecution, new LinkedList<>(), new LinkedHashSet<>());
	}

	protected void calculateForks(final MavenSession session, final MavenProject project,
			final MojoExecution mojoExecution, final Collection<Fork> alreadyForkedForks,
			final Collection<MojoDescriptor> alreadyForkedMojos)
			throws MojoNotFoundException, PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
			LifecyclePhaseNotFoundException, LifecycleNotFoundException, PluginVersionResolutionException {
		MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

		boolean isForkingGoal = false;
		if (mojoDescriptor.isForking())
			isForkingGoal = isBlank(mojoDescriptor.getExecutePhase());

		final String selectedLifecycleTaskReqCalc = this.settings.getLifecycleTaskRequestCalculatorOnFork()
				.getSelected();
		final LifecycleTaskRequestCalculatorDelegate lifecycleTaskReqCalcDelegate = selectLifecycleTaskReqCalcDelegate(
				selectedLifecycleTaskReqCalc);

		Fork fork = null;
		ExecutionArchiveSelectorConfig selectorConfig = null;
		if (mojoExecution instanceof MojoExecutionData) {
			MojoExecutionData data = (MojoExecutionData) mojoExecution;
			fork = data.getFork();
			selectorConfig = data.getExecutionArchiveSelectorConfig();
		} else if (!isForkingGoal) {
			// generate a Fork from MojoDescriptor in case some other extension injected a
			// mojoExecution missing the MojoExecutionData interface
			fork = new Fork();
			String lifecycleId = mojoDescriptor.getExecuteLifecycle();
			String phaseId = mojoDescriptor.getExecutePhase();
			if (isBlank(lifecycleId)) {
				Lifecycle lifecycle = this.defaultLifeCycles.get(phaseId);
				List<TargetPhase> phases = new LinkedList<>();
				for (String phase : lifecycleTaskReqCalcDelegate.calculateTaskRequest(lifecycle, phaseId)
						.getPhaseSequence())
					phases.add(new TargetPhase(phase));
				fork.setPhases(phases);
			} else {
				TargetLifecycle lifecycle = new TargetLifecycle(lifecycleId);
				if (!isBlank(phaseId))
					lifecycle.setStopPhase(phaseId);
				fork.setLifecycle(lifecycle);
			}
			selectorConfig = this.selectorConfigFactory.createEmptyConfig()
					.selectActiveProject(project)
					.selectPackagingProcedure(project.getPackaging())
					.selectModes("default")
					.selectActiveExecutions(mojoExecution.getExecutionId());
		}

		if (isForkingGoal) {
			if (!alreadyForkedMojos.add(mojoDescriptor))
				return;
		} else {
			if (fork == null || !fork.isValid() || alreadyForkedForks.contains(fork))
				return;
			alreadyForkedForks.add(fork);

			if (this.settings.getPatchMojoOnFork()
					.getSelected())
				mojoExecution.setMojoDescriptor(patchMojoDescriptorForLogging(mojoDescriptor, fork));
		}

		List<MavenProject> forkedProjects = LifecycleDependencyResolver.getProjects(project, session,
				mojoDescriptor.isAggregator());

		for (MavenProject forkedProject : forkedProjects) {
			if (forkedProject != project) {
				lifecyclePluginResolver.resolveMissingPluginVersions(forkedProject, session);
			}

			List<MojoExecution> forkedExecutions;

			if (isForkingGoal) {
				forkedExecutions = calculateForkedGoal(session, forkedProject, mojoExecution, alreadyForkedForks,
						alreadyForkedMojos);
			} else {
				forkedExecutions = calculateForkedLifecycle(session, forkedProject, mojoExecution, fork, selectorConfig,
						alreadyForkedForks, alreadyForkedMojos);
			}

			mojoExecution.setForkedExecutions(BuilderCommon.getKey(forkedProject), forkedExecutions);
		}

		if (isForkingGoal)
			alreadyForkedMojos.remove(mojoDescriptor);
		else
			alreadyForkedForks.remove(fork);
	}

	protected MojoDescriptor patchMojoDescriptorForLogging(final MojoDescriptor mojoDescriptor, final Fork fork) {
		MojoDescriptor patched = mojoDescriptor.clone();
		String start = null;
		String stop = null;

		List<TargetPhase> phases = fork.getPhases();
		if (phases != null && !phases.isEmpty()) {
			for (TargetPhase phase : phases) {
				if (start == null)
					start = phase.getId();
				stop = phase.getId();
			}
		} else {
			TargetLifecycle targetLifecycle = fork.getLifecycle();
			if (targetLifecycle != null) {
				start = targetLifecycle.getStartPhase();
				stop = targetLifecycle.getStopPhase();
			}
			boolean findStop = stop == null;
			if (start == null || findStop)
				for (String phase : this.lifecycles.get(targetLifecycle.getId())
						.getPhases()) {
					if (start == null)
						start = phase;
					if (findStop)
						stop = phase;
					else
						continue;
				}
		}

		patched.setExecuteLifecycle(fork.getLifecycleId());
		patched.setExecutePhase(start == stop ? start : "(" + start + "," + stop + ")");
		return patched;
	}

	protected List<MojoExecution> calculateForkedLifecycle(final MavenSession session, final MavenProject project,
			final MojoExecution mojoExecution, final Fork fork, final ExecutionArchiveSelectorConfig baseSelectorConfig,
			final Collection<Fork> alreadyForkedForks, final Collection<MojoDescriptor> alreadyForkedMojos)
			throws MojoNotFoundException, PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
			LifecyclePhaseNotFoundException, LifecycleNotFoundException, PluginVersionResolutionException {
		ExecutionArchiveSelectorConfig cnf = this.selectorConfigFactory.createEmptyConfig();

		// select mvnProject
		cnf.selectActiveProject(project);
		// select packaging-procedure
		cnf.selectPackagingProcedure(baseSelectorConfig.getPackagingProcedure());
		// select modes
		if (isBlank(fork.getMode()))
			cnf.selectModes(baseSelectorConfig.getModes());
		else
			cnf.selectModes(fork.getMode());
		// select executions
		cnf.selectActiveExecutions(fork.getExecutions());

		Map<String, List<MojoExecution>> lifecycleMappings = calculateForkMappings(session, project, fork, cnf);

		for (List<MojoExecution> forkedExecutions : lifecycleMappings.values()) {
			for (MojoExecution forkedExecution : forkedExecutions) {
				if (forkedExecution.getMojoDescriptor() == null) {
					MojoDescriptor forkedMojoDescriptor = this.pluginManager.getMojoDescriptor(
							forkedExecution.getPlugin(), forkedExecution.getGoal(),
							project.getRemotePluginRepositories(), session.getRepositorySession());

					forkedExecution.setMojoDescriptor(forkedMojoDescriptor);
				}

				selectExecutionConfigurator(mojoExecution).configure(project, forkedExecution, false);
			}
		}

		// seed configuration with plugin-lifecycle specific settings
		// (defined by the plugin that forks & defines configs for lifecycle/phase)
		injectLifecycleOverlay(lifecycleMappings, session, project, mojoExecution, fork.getLifecycleId());

		List<MojoExecution> mappings = new LinkedList<>();
		for (Entry<String, List<MojoExecution>> entry : lifecycleMappings.entrySet())
			if (entry.getValue() != null)
				for (MojoExecution exec : entry.getValue()) {
					if (this.settings.getGeneratePluginExecutionsOnFork()
							.getSelected())
						generatePluginExecutions(project, exec);
					// complete with default values
					finalizeMojoConfiguration(exec);
					calculateForks(session, project, exec, alreadyForkedForks, alreadyForkedMojos);
					mappings.add(exec);
				}
		return mappings;
	}

	protected List<String> calculateExecutingPhasesByFork(final ExecutionArchiveSelection selection, final Fork fork) {
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
				this.log.warn(String.format(WARN_SKIPPING_UNKNOWN_LIFECYCLE, Runes4MavenProperties.PREFIX_ID,
						targetLifecycle.getId()));
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
			MojoNotFoundException, InvalidPluginDescriptorException, LifecyclePhaseNotFoundException {

		final ExecutionArchiveSelection selection = this.selector.compileSelection(cnf);
		List<String> orderedPhases = calculateExecutingPhasesByFork(selection, fork);

		// Initialize mapping from lifecycle phase to bound mojos.
		Map<String, List<MojoExecution>> phaseToMojoMapping = new LinkedHashMap<>();
		for (String phase : orderedPhases) {

			Lifecycle lifecycle = this.defaultLifeCycles.get(phase);
			if (lifecycle == null)
				throw new LifecyclePhaseNotFoundException(
						String.format(ERR_UNKNOWN_LIFECYCLE_PHASE_ON_FORK, Runes4MavenProperties.PREFIX_ID, phase),
						phase);

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
		Map<String, List<MojoExecution>> lifecycleMappings = new LinkedHashMap<>();

		for (String phase : orderedPhases)
			lifecycleMappings.put(phase, phaseToMojoMapping.get(phase));

		return lifecycleMappings;
	}

	protected void injectLifecycleOverlay(final Map<String, List<MojoExecution>> lifecycleMappings,
			final MavenSession session, final MavenProject project, final MojoExecution mojoExecution,
			String forkedLifecycleId) throws MojoNotFoundException, PluginNotFoundException, PluginResolutionException,
			PluginDescriptorParsingException, NoPluginFoundForPrefixException, InvalidPluginDescriptorException,
			PluginVersionResolutionException {
		MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();
		PluginDescriptor pluginDescriptor = mojoDescriptor.getPluginDescriptor();

		if (isBlank(forkedLifecycleId))
			forkedLifecycleId = mojoDescriptor.getExecuteLifecycle();
		if (isBlank(forkedLifecycleId))
			return;

		org.apache.maven.plugin.lifecycle.Lifecycle lifecycleOverlay;

		try {
			lifecycleOverlay = pluginDescriptor.getLifecycleMapping(forkedLifecycleId);
		} catch (IOException | XmlPullParserException e) {
			return;
		}

		if (lifecycleOverlay == null)
			return;

		if (mojoExecution instanceof MojoExecutionData)
			((MojoExecutionData) mojoExecution).setLifecycleOverlayOrigin(pluginDescriptor);

		for (Phase phase : lifecycleOverlay.getPhases()) {
			List<MojoExecution> forkedExecutions = lifecycleMappings.get(phase.getId());

			if (forkedExecutions == null)
				continue;

			Map<MojoDescriptor, Map<String, Object>> mappedConfigurations = new LinkedHashMap<>();
			for (Execution execution : phase.getExecutions()) {
				Object executionConfiguration = execution.getConfiguration();

				for (String goal : execution.getGoals()) {
					String executionId = null;
					int splitIdx = goal.indexOf('@');
					if (0 < splitIdx)
						executionId = goal.substring(splitIdx + 1);

					MojoDescriptor forkedMojoDescriptor;
					if (goal.indexOf(':') < 0) {
						forkedMojoDescriptor = pluginDescriptor.getMojo(goal);
						if (forkedMojoDescriptor == null)
							throw new MojoNotFoundException(goal, pluginDescriptor);
					} else
						forkedMojoDescriptor = this.mojoDescriptorCreator.getMojoDescriptor(goal, session, project);

					Map<String, Object> configurations = mappedConfigurations.get(forkedMojoDescriptor);
					if (configurations == null) {
						configurations = new LinkedHashMap<>();
						mappedConfigurations.put(forkedMojoDescriptor, configurations);
					}
					configurations.put(executionId, executionConfiguration);
				}
			}

			for (MojoExecution mojoExec : forkedExecutions) {
				MojoDescriptor mojoDesc = mojoExec.getMojoDescriptor();
				Map<String, Object> configurations = mappedConfigurations.get(mojoDesc);
				if (configurations == null)
					continue;
				Object configuration = configurations.get(mojoExec.getExecutionId());
				if (configuration == null)
					configuration = configurations.get(null);
				if (configuration == null || !(configuration instanceof Xpp3Dom))
					continue;
				mojoExec.setConfiguration((Xpp3Dom) configuration);
				selectExecutionConfigurator(mojoExec).configure(project, mojoExec, true);
			}

			Xpp3Dom phaseConfiguration = (Xpp3Dom) phase.getConfiguration();
			if (phaseConfiguration != null)
				for (MojoExecution forkedExecution : forkedExecutions) {
					Xpp3Dom forkedConfiguration = forkedExecution.getConfiguration();

					forkedConfiguration = Xpp3Dom.mergeXpp3Dom(phaseConfiguration, forkedConfiguration);

					forkedExecution.setConfiguration(forkedConfiguration);
				}
		}
	}

	// org.apache.maven.plugins:maven-remote-resources-plugin:1.0:process
	// take repo mans into account as one may be aggregating prefixes of many
	// collect at the root of the repository, read the one at the root, and
	// fetch remote if something is missing
	// or the user forces the issue

	protected List<MojoExecution> calculateForkedGoal(final MavenSession session, final MavenProject project,
			final MojoExecution mojoExecution, final Collection<Fork> alreadyForkedForks,
			final Collection<MojoDescriptor> alreadyForkedMojos)
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

		if (alreadyForkedMojos.contains(forkedMojoDescriptor)) {
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

		if (this.settings.getGeneratePluginExecutionsOnFork()
				.getSelected())
			generatePluginExecutions(project, forkedExecution);

		selectExecutionConfigurator(forkedExecution).configure(project, forkedExecution, true);

		finalizeMojoConfiguration(forkedExecution);

		calculateForks(session, project, forkedExecution, alreadyForkedForks, alreadyForkedMojos);

		return Collections.singletonList(forkedExecution);
	}

	protected LifecycleTaskRequestCalculatorDelegate selectLifecycleTaskReqCalcDelegate(String hint)
			throws LifecyclePhaseNotFoundException {
		final LifecycleTaskRequestCalculatorDelegate lifecycleTaskReqCalcDelegate = this.lifecycleTaskRequestDelegates
				.get(hint);
		if (lifecycleTaskReqCalcDelegate == null)
			throw new LifecyclePhaseNotFoundException(
					"[r4m] LifecycleTaskRequestCalculatorDelegate<" + hint + "> not found!", null);
		return lifecycleTaskReqCalcDelegate;
	}

	protected MojoExecutionConfigurator selectExecutionConfigurator(MojoExecution mojoExecution) {
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
