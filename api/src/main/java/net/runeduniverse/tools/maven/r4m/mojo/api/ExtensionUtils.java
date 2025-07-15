/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.mojo.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.api.Settings;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.Phase;

import static net.runeduniverse.lib.utils.maven3.PluginUtils.getVersionFromArtifact;

public interface ExtensionUtils {

	public static void warnExtensionFeatureState(final Log log) {
		log.warn("╔══════════════════════════════════════════════════════════════════════╗");
		log.warn("║  R4M is not loaded correctly! - Most functionality is unavailable!   ║");
		log.warn("╟──────────────────────────────────────────────────────────────────────╢");
		log.warn("║  Enable all features by loading R4M as a core-extension!             ║");
		log.warn("╚══════════════════════════════════════════════════════════════════════╝");
	}

	public static void mojoFailureExtensionLoading(final Log log) throws MojoFailureException {
		log.error("");
		log.error("  R4M is not correctly loaded, check your configuration!");
		log.error("");
		throw new MojoFailureException("R4M is not correctly loaded!");
	}

	public static boolean supportsExtensionFeatures(final Settings settings) {
		if (settings == null)
			return false;
		switch (settings.getLoadState()) {
		case SYSTEM_EXTENSION:
		case CORE_EXTENSION:
			return true;
		case BUILD_EXTENSION:
		case PLUGIN:
		default:
			return false;
		}
	}

	public static <T extends Mojo> String getR4MVersionFromArtifact(final Class<T> clazz, final Log log) {
		return getVersionFromArtifact(clazz, log, Runes4MavenProperties.GROUP_ID, Runes4MavenProperties.ARTIFACT_ID);
	}

	public static String asVersionTag(final String version) {
		if (version == null)
			return "";
		return " (v" + version.trim() + ")";
	}

	@SuppressWarnings("deprecation")
	public static ExecutionArchive acquireExecutionArchive(final MavenSession mvnSession, final ClassRealm realm) {
		final ClassRealm extRealm = realm.getWorld()
				.getClassRealm(Runes4MavenProperties.BUILD_EXTENSION_REALM_ID);
		if (extRealm == null)
			return null;

		ExecutionArchive archive;
		try {
			Thread.currentThread()
					.setContextClassLoader(extRealm);

			archive = mvnSession.getContainer()
					.lookup(ExecutionArchive.class);

			Thread.currentThread()
					.setContextClassLoader(realm);
		} catch (ComponentLookupException e) {
			archive = null;
		}
		return archive;
	}

	@SuppressWarnings("deprecation")
	public static GoalRequirementArchive acquireGoalRequirementArchive(final MavenSession mvnSession,
			final ClassRealm realm) {
		final ClassRealm extRealm = realm.getWorld()
				.getClassRealm(Runes4MavenProperties.BUILD_EXTENSION_REALM_ID);
		if (extRealm == null)
			return null;

		GoalRequirementArchive archive;
		try {
			Thread.currentThread()
					.setContextClassLoader(extRealm);

			archive = mvnSession.getContainer()
					.lookup(GoalRequirementArchive.class);

			Thread.currentThread()
					.setContextClassLoader(realm);
		} catch (ComponentLookupException e) {
			archive = null;
		}
		return archive;
	}

	/***
	 * Reduces the {@link Execution} collection by compiling the intersections
	 * between the goals and summarizing the remaining.
	 *
	 * <p>
	 * Mergeable Goals will be merged using {@link #createEquivalent(Goal)}, note
	 * the {@link Fork} instance will not be cloned!
	 *
	 * @param executions the {@link Collection} of {@link Execution} instances to be
	 *                   reduced
	 * @throws NullPointerException if {@code executions} is {@code null}
	 */
	public static void reduce(final Collection<Execution> executions) {
		Objects.requireNonNull(executions);
		if (executions.size() < 2)
			return;

		synchronized (executions) {
			final Set<Execution> mergeCol = new LinkedHashSet<>();
			final List<Execution> execCol = new LinkedList<>(executions);
			for (Execution origExec : executions) {
				// don't merge it with itself
				if (execCol.contains(origExec))
					execCol.remove(origExec);
				else
					// cant find it -> already merged
					continue;
				// check if special condition is active!
				final boolean matchRestrictions = origExec.getRestrictions()
						.isEmpty();
				boolean gotReduced = false;

				for (ListIterator<Execution> iExec = execCol.listIterator(); iExec.hasNext();) {
					final Execution exec = iExec.next();

					if (!isSimilar(origExec, exec, false))
						continue;
					if (matchRestrictions)
						if (!exec.getRestrictions()
								.isEmpty())
							continue;

					final Execution reduced = merge(origExec, exec, false);
					final boolean isReduced = !reduced.getLifecycles()
							.isEmpty();

					if (exec.getLifecycles()
							.isEmpty())
						iExec.remove();
					if (!gotReduced && !origExec.getLifecycles()
							.isEmpty())
						iExec.add(origExec);
					if (isReduced) {
						origExec = reduced;
						gotReduced = true;
					}
				}
				mergeCol.add(origExec);
			}
			executions.clear();
			// implicit unique reduction
			mergeCol.addAll(execCol);
			execCol.clear();
			execCol.addAll(mergeCol);
			for (Execution remExec : mergeCol) {
				// don't merge it with itself
				if (execCol.contains(remExec))
					execCol.remove(remExec);
				else // cant find it -> already merged
					continue;

				for (ListIterator<Execution> iExec = execCol.listIterator(); iExec.hasNext();) {
					final Execution exec = iExec.next();

					if (!isSimilar(remExec, exec, true))
						continue;

					remExec = merge(remExec, exec, true);
					iExec.remove();
				}
				if (!remExec.getLifecycles()
						.isEmpty())
					executions.add(remExec);
			}
		}
	}

	/***
	 * Merges a dominant {@link Execution} and a secondary {@link Execution}, both
	 * must not be {@code null}, by creating an equivalent instance of
	 * {@code domExec} using {@link #createEquivalent(Execution)}.
	 *
	 * <p>
	 * By default only goals contained by both executions will be carried over to
	 * the merged execution. This behavior may be changed by setting the
	 * {@code force} flag, which will force all goals included in both executions to
	 * be carried over. Please keep in mind that all moved goals will be removed
	 * from their previous phases. Furthermore all empty phases and subsequently
	 * empty lifecycles will be removed too.
	 *
	 * <p>
	 * All goals contained by both executions will be merged into an equivalent
	 * instance, created using {@link #createEquivalent(Goal)}. Additionally their
	 * modes will also be merged.
	 *
	 * @param domExec the dominant {@link Execution}
	 * @param secExec the secondary {@link Execution}
	 * @param force   following flag may be set to force all goals to be merged
	 * @return an equivalent instance of {@code domExec} merged with {@code secExec}
	 * @throws NullPointerException if either {@code domExec} or {@code secExec} is
	 *                              {@code null}
	 */
	public static Execution merge(final Execution domExec, final Execution secExec, boolean force) {
		Objects.requireNonNull(domExec);
		Objects.requireNonNull(secExec);

		final Execution mergeExecution = createEquivalent(domExec);
		mergeExecution.addRestrictions(secExec.getRestrictions());

		for (Iterator<Lifecycle> iDomLifecycle = domExec.getLifecycles()
				.values()
				.iterator(); iDomLifecycle.hasNext();) {
			final Lifecycle domLifecycle = iDomLifecycle.next();
			Lifecycle secLifecycle = secExec.getLifecycle(domLifecycle.getId());
			if (secLifecycle == null)
				if (force)
					secLifecycle = new Lifecycle(domLifecycle.getId());
				else
					continue;
			Lifecycle mergeLifecycle = mergeExecution.getLifecycle(domLifecycle.getId());
			if (mergeLifecycle == null)
				mergeLifecycle = new Lifecycle(domLifecycle.getId());

			for (Iterator<Phase> iDomPhase = domLifecycle.getPhases()
					.values()
					.iterator(); iDomPhase.hasNext();) {
				final Phase domPhase = iDomPhase.next();
				Phase secPhase = secLifecycle.getPhase(domPhase.getId());
				if (secPhase == null)
					if (force)
						secPhase = new Phase(domPhase.getId());
					else
						continue;
				Phase mergePhase = mergeLifecycle.getPhase(domPhase.getId());
				if (mergePhase == null)
					mergePhase = new Phase(domPhase.getId());

				merge(domPhase, secPhase, mergePhase);

				if (force) {
					mergePhase.addGoals(domPhase.getGoals());
					domPhase.getGoals()
							.clear();
					mergePhase.addGoals(secPhase.getGoals());
					secPhase.getGoals()
							.clear();
				}

				if (secPhase.getGoals()
						.isEmpty())
					secLifecycle.getPhases()
							.remove(secPhase.getId());
				if (domPhase.getGoals()
						.isEmpty())
					iDomPhase.remove();
				if (!mergePhase.getGoals()
						.isEmpty())
					mergeLifecycle.putPhase(mergePhase);
			}
			if (secLifecycle.getPhases()
					.isEmpty())
				secExec.getLifecycles()
						.remove(secLifecycle.getId());
			if (domLifecycle.getPhases()
					.isEmpty())
				iDomLifecycle.remove();
			if (!mergeLifecycle.getPhases()
					.isEmpty())
				mergeExecution.putLifecycle(mergeLifecycle);
		}

		if (force)
			for (Iterator<Lifecycle> iSecLifecycle = secExec.getLifecycles()
					.values()
					.iterator(); iSecLifecycle.hasNext();) {
				final Lifecycle secLifecycle = iSecLifecycle.next();
				Lifecycle mergeLifecycle = mergeExecution.getLifecycle(secLifecycle.getId());
				if (mergeLifecycle == null)
					mergeLifecycle = new Lifecycle(secLifecycle.getId());

				for (Iterator<Phase> iSecPhase = secLifecycle.getPhases()
						.values()
						.iterator(); iSecPhase.hasNext();) {
					final Phase secPhase = iSecPhase.next();
					Phase mergePhase = mergeLifecycle.getPhase(secPhase.getId());
					if (mergePhase == null)
						mergePhase = new Phase(secPhase.getId());

					mergePhase.addGoals(secPhase.getGoals());
					iSecPhase.remove();

					if (!mergePhase.getGoals()
							.isEmpty())
						mergeLifecycle.putPhase(mergePhase);
				}
				iSecLifecycle.remove();
				if (!mergeLifecycle.getPhases()
						.isEmpty())
					mergeExecution.putLifecycle(mergeLifecycle);
			}
		return mergeExecution;
	}

	/***
	 * Merges all {@link Goal} instances contained in the dominant and secondary
	 * {@link Phase} into the merge {@link Phase}. Additionally their modes will be
	 * merged.
	 *
	 * @param domPhase   the dominant {@link Phase}
	 * @param secPhase   the secondary {@link Phase}
	 * @param mergePhase the merge {@link Phase}
	 */
	public static void merge(final Phase domPhase, final Phase secPhase, final Phase mergePhase) {
		for (Iterator<Goal> iDomGoal = domPhase.getGoals()
				.iterator(); iDomGoal.hasNext();) {
			final Goal domGoal = iDomGoal.next();
			for (Iterator<Goal> iSecGoal = secPhase.getGoals()
					.iterator(); iSecGoal.hasNext();) {
				final Goal secGoal = iSecGoal.next();
				if (!isSimilar(domGoal, secGoal, false))
					continue;
				final Goal mergeGoal = createEquivalent(domGoal);
				mergeGoal.addModes(secGoal.getModes());
				mergePhase.addGoal(mergeGoal);
				iSecGoal.remove();
				iDomGoal.remove();
			}
		}
	}

	public static boolean isSimilar(final Execution origExec, final Execution exec, final boolean checkRestrictions) {
		// id
		final String id = origExec.getId();
		if (id == null) {
			if (exec.getId() != null)
				return false;
		} else if (!id.equals(exec.getId()))
			return false;
		// source
		final ExecutionSource source = origExec.getSource();
		if (source == null) {
			if (exec.getSource() != null)
				return false;
		} else if (!source.equals(exec.getSource()))
			return false;
		// inherited & active flags
		if ((origExec.isInherited() != exec.isInherited()) || origExec.isAlwaysActive() != exec.isAlwaysActive()
				|| origExec.isDefaultActive() != exec.isDefaultActive()
				|| origExec.isNeverActive() != exec.isNeverActive())
			return false;
		// restrictions
		if (checkRestrictions) {
			final List<ExecutionRestriction> execRestrictions = new LinkedList<>(exec.getRestrictions());
			if (execRestrictions.size() != origExec.getRestrictions()
					.size())
				return false;
			if (!execRestrictions.containsAll(origExec.getRestrictions()))
				return false;
		}
		// trigger
		final List<?> execTrigger = new LinkedList<>(exec.getTrigger());
		if (execTrigger.size() != origExec.getTrigger()
				.size())
			return false;
		if (!execTrigger.containsAll(origExec.getTrigger()))
			return false;
		// equal metadata
		return true;
	}

	public static boolean isSimilar(final Goal origGoal, final Goal goal, final boolean checkModes) {
		if (origGoal == goal)
			return true;

		if (origGoal.getGroupId() == null) {
			if (goal.getGroupId() != null)
				return false;
		} else if (!origGoal.getGroupId()
				.equals(goal.getGroupId()))
			return false;

		if (origGoal.getArtifactId() == null) {
			if (goal.getArtifactId() != null)
				return false;
		} else if (!origGoal.getArtifactId()
				.equals(goal.getArtifactId()))
			return false;

		if (origGoal.getGoalId() == null) {
			if (goal.getGoalId() != null)
				return false;
		} else if (!origGoal.getGoalId()
				.equals(goal.getGoalId()))
			return false;

		if (origGoal.getOptional() == null) {
			if (goal.getOptional() != null)
				return false;
		} else if (!origGoal.getOptional()
				.equals(goal.getOptional()))
			return false;

		if (checkModes && !(origGoal.getModes()
				.size() == goal.getModes()
						.size()
				&& origGoal.getModes()
						.containsAll(goal.getModes())))
			return false;

		if (origGoal.getFork() != null)
			return origGoal.getFork()
					.equals(goal.getFork());

		return true;
	}

	public static Execution createEquivalent(final Execution original) {
		final Execution equivalent = new Execution(original.getId(), original.getSource());
		equivalent.setInherited(original.isInherited());
		equivalent.setAlwaysActive(original.isAlwaysActive());
		equivalent.setDefaultActive(original.isDefaultActive());
		equivalent.setNeverActive(original.isNeverActive());
		equivalent.addRestrictions(original.getRestrictions());
		equivalent.getTrigger()
				.addAll(original.getTrigger());
		return equivalent;
	}

	public static Goal createEquivalent(final Goal original) {
		final Goal equivalent = new Goal(original.getGroupId(), original.getArtifactId(), original.getGoalId());
		equivalent.addModes(original.getModes());
		equivalent.setOptional(original.getOptional());
		equivalent.setFork(original.getFork());
		return equivalent;
	}

	public static void replaceWithEquivalents(final Set<Execution> executions) {
		final Set<Execution> equivalents = new LinkedHashSet<>();
		for (Execution exec : executions) {
			final Execution equivalentExec = createEquivalent(exec);
			for (Lifecycle lifecycle : exec.getLifecycles()
					.values()) {
				final Lifecycle equivalentLifecycle = new Lifecycle(lifecycle.getId());
				equivalentExec.putLifecycle(equivalentLifecycle);
				for (Phase phase : lifecycle.getPhases()
						.values()) {
					final Phase equivalentPhase = new Phase(phase.getId());
					equivalentLifecycle.putPhase(equivalentPhase);
					equivalentPhase.addGoals(phase.getGoals());
				}
			}
			equivalents.add(equivalentExec);
		}
		executions.clear();
		executions.addAll(equivalents);
	}

}
