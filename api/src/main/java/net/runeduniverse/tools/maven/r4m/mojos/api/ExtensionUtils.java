package net.runeduniverse.tools.maven.r4m.mojos.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;
import net.runeduniverse.tools.maven.r4m.pem.model.Goal;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;
import net.runeduniverse.tools.maven.r4m.pem.model.Phase;

public interface ExtensionUtils {

	public static void mojoFailureExtensionLoading(final Log log) throws MojoFailureException {
		log.error("");
		log.error("  R4M is not loaded as extension!");
		log.error("");
		log.error("  Please check your configuration!");
		log.error("");
		log.error("  Example Build-Extension inclusion:");
		log.error("");
		log.error("  <plugin>");
		log.error("      <groupId>net.runeduniverse.tools.maven.r4m</groupId>");
		log.error("      <artifactId>r4m-maven-extension</artifactId>");
		log.error("      <version>[0.0.0,)</version>");
		log.error("      <extensions>true</extensions>");
		log.error("  </plugin>");
		log.error("");
		throw new MojoFailureException("R4M is not loaded as extension!");
	}

	@SuppressWarnings("deprecation")
	public static ExecutionArchive acquireExecutionArchive(final MavenSession mvnSession, final ClassRealm realm) {
		ClassRealm extRealm = realm.getWorld()
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
				boolean matchRestrictions = origExec.getRestrictions()
						.isEmpty();
				boolean gotReduced = false;

				for (ListIterator<Execution> iExec = execCol.listIterator(); iExec.hasNext();) {
					Execution exec = (Execution) iExec.next();

					if (!isSimilar(origExec, exec, false))
						continue;
					if (matchRestrictions)
						if (!exec.getRestrictions()
								.isEmpty())
							continue;

					Execution reduced = merge(origExec, exec, false);
					boolean isReduced = !reduced.getLifecycles()
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
					Execution exec = (Execution) iExec.next();

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

		Execution mergeExecution = createEquivalent(domExec);
		mergeExecution.addRestrictions(secExec.getRestrictions());

		for (Iterator<Lifecycle> iDomLifecycle = domExec.getLifecycles()
				.values()
				.iterator(); iDomLifecycle.hasNext();) {
			Lifecycle domLifecycle = (Lifecycle) iDomLifecycle.next();
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
				Phase domPhase = (Phase) iDomPhase.next();
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
				Lifecycle secLifecycle = iSecLifecycle.next();
				Lifecycle mergeLifecycle = mergeExecution.getLifecycle(secLifecycle.getId());
				if (mergeLifecycle == null)
					mergeLifecycle = new Lifecycle(secLifecycle.getId());

				for (Iterator<Phase> iSecPhase = secLifecycle.getPhases()
						.values()
						.iterator(); iSecPhase.hasNext();) {
					Phase secPhase = iSecPhase.next();
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
			Goal domGoal = iDomGoal.next();
			for (Iterator<Goal> iSecGoal = secPhase.getGoals()
					.iterator(); iSecGoal.hasNext();) {
				Goal secGoal = iSecGoal.next();
				if (!isSimilar(domGoal, secGoal, false))
					continue;
				Goal mergeGoal = createEquivalent(domGoal);
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
		// inherited
		if (origExec.isInherited() != exec.isInherited())
			return false;
		// active flags
		if (origExec.isAlwaysActive() != exec.isAlwaysActive() || origExec.isDefaultActive() != exec.isDefaultActive()
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
		final List<ExecutionTrigger> execTrigger = new LinkedList<>(exec.getTrigger());
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
		Execution equivalent = new Execution(original.getId(), original.getSource());
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
		Goal equivalent = new Goal(original.getGroupId(), original.getArtifactId(), original.getGoalId());
		equivalent.addModes(original.getModes());
		equivalent.setFork(original.getFork());
		return equivalent;
	}

	public static void replaceWithEquivalents(final Set<Execution> executions) {
		final Set<Execution> equivalents = new LinkedHashSet<>();
		for (Execution exec : executions) {
			Execution equivalentExec = createEquivalent(exec);
			for (Lifecycle lifecycle : exec.getLifecycles()
					.values()) {
				Lifecycle equivalentLifecycle = new Lifecycle(lifecycle.getId());
				equivalentExec.putLifecycle(equivalentLifecycle);
				for (Phase phase : lifecycle.getPhases()
						.values()) {
					Phase equivalentPhase = new Phase(phase.getId());
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
