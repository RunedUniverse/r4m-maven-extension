package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecycleMappingDelegate;
import org.apache.maven.lifecycle.internal.DefaultLifecycleMappingDelegate;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.maven.r4m.Properties;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = Properties.LIFECYCLE.DEV.LIFECYCLE_PARTICIPANT_HINT)
public class DevMavenLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	public static final String ERR_FAILED_LOADING_MAVEN_EXTENSION_CLASSREALM = "Failed loading maven-extension ClassRealm";

	@Requirement
	private Logger log;
	@Requirement
	private PlexusContainer container;
	@Requirement(role = Lifecycle.class)
	private Map<String, Lifecycle> lifecycles;
	@Requirement(role = LifecycleMappingDelegate.class)
	private Map<String, LifecycleMappingDelegate> mappedDelegates;

	private boolean coreExtension = false;

	/**
	 * Invoked after MavenSession instance has been created.
	 *
	 * This callback is intended to allow extensions to inject execution properties,
	 * activate profiles and perform similar tasks that affect MavenProject instance
	 * construction.
	 */
	// TODO This is too early for build extensions, so maybe just remove it?
	public void afterSessionStart(MavenSession session) throws MavenExecutionException {
		this.coreExtension = true;
		// only gets called when loaded as core-extension
	}

	/**
	 * Invoked after all MavenProject instances have been created.
	 *
	 * This callback is intended to allow extensions to manipulate MavenProjects
	 * before they are sorted and actual build execution starts.
	 */
	public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
		ClassLoader currentClassLoader = Thread.currentThread()
				.getContextClassLoader();
		try {
			if (this.coreExtension) {
				ClassRealm mavenExtRealm = this.container.getContainerRealm()
						.getWorld()
						.getRealm("maven.ext");
				Thread.currentThread()
						.setContextClassLoader(mavenExtRealm);
			}

			log.debug("Generating DEV Phases:");

			Lifecycle devLifecycle = null;

			devLifecycle = lifecycles.get("dev");
			devLifecycle.getPhases()
					.clear();

			for (Lifecycle lifecycle : lifecycles.values()) {
				if (lifecycle == devLifecycle)
					continue;

				// ignore specifically mapped lifecycles except default
				if (!lifecycle.getId()
						.equals(DefaultLifecycleMappingDelegate.HINT) && mappedDelegates.containsKey(lifecycle.getId()))
					continue;

				List<String> devPhases = new LinkedList<>();
				for (String phase : lifecycle.getPhases()) {
					phase = "dev-" + phase;
					devPhases.add(phase);
					devLifecycle.getPhases()
							.add(phase);
				}
				log.debug(lifecycle.getId() + " -> [" + String.join(", ", devPhases) + "]");
			}
		} catch (NoSuchRealmException e) {
			throw new MavenExecutionException(ERR_FAILED_LOADING_MAVEN_EXTENSION_CLASSREALM, e);
		} finally {
			Thread.currentThread()
					.setContextClassLoader(currentClassLoader);
		}
	}

	/**
	 * Invoked after all projects were built.
	 *
	 * This callback is intended to allow extensions to perform cleanup of any
	 * allocated external resources after the build. It is invoked on best-effort
	 * basis and may be missed due to an Error or RuntimeException in Maven core
	 * code.
	 * 
	 * @since 3.2.1, MNG-5389
	 */
	public void afterSessionEnd(MavenSession session) throws MavenExecutionException {
		// only gets called when loaded as core-extension
	}
}