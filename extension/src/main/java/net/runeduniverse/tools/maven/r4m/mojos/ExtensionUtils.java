package net.runeduniverse.tools.maven.r4m.mojos;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchive;

public class ExtensionUtils {

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
}
