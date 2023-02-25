package net.runeduniverse.tools.maven.r4m.mojos;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

public class ExtensionUtils {

	public static void mojoFailureExtensionLoading(Log log) throws MojoFailureException {
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
}
