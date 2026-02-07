/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.mojo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import net.runeduniverse.lib.utils.maven3.ext.api.ExtensionTool;
import net.runeduniverse.tools.maven.r4m.R4MProperties;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

/**
 * Configures the r4m-maven-extension as a core-extension on this Maven Project.
 *
 * @goal setup
 * @requiresProject false
 * @requiresReports false
 * @threadSafe false
 * @since 1.1.0
 * @author VenaNocta
 */
public class SetupMojo extends AbstractMojo {

	public static final String POM_PROP_FILE_TEMPLATE = "META-INF/maven/%s/%s/pom.properties";

	/**
	 * @parameter default-value="${session}"
	 * @readonly
	 */
	private MavenSession mvnSession;
	/**
	 * @parameter default-value="${project}"
	 * @readonly
	 */
	private MavenProject mvnProject;

	/**
	 * @component
	 */
	private ExtensionTool tool;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final Properties p = new Properties();
		try (final InputStream stream = getClass().getClassLoader()
				.getResourceAsStream(
						String.format(POM_PROP_FILE_TEMPLATE, R4MProperties.GROUP_ID, R4MProperties.ARTIFACT_ID))) {
			if (stream != null) {
				p.load(stream);
			}
		} catch (IOException | IllegalArgumentException ignored) {
			throw new MojoExecutionException("Failed to load pom.properties from Mojo source!", ignored);
		}

		final String groupId = p.getProperty("groupId");
		final String artifactId = p.getProperty("artifactId");
		final String version = p.getProperty("version");

		if (isBlank(groupId) || isBlank(artifactId) || isBlank(version))
			throw new MojoExecutionException("Failed to load pom.properties from Mojo source!");

		final File topLevelPomFile = this.mvnSession.getTopLevelProject()
				.getFile();
		if (topLevelPomFile == null || !topLevelPomFile.exists())
			throw new MojoFailureException("r4m:setup requires a Maven-Project with an existing POM-File (pom.xml)!");

		try {
			if (!this.tool.setupAsCoreExtension(this.mvnSession, this.mvnProject, groupId.trim(), artifactId.trim(),
					version.trim()))
				throw new MojoFailureException(
						"Unexpected Exception during execution of core-extension setup, see error log!");
		} catch (IOException e) {
			throw new MojoFailureException(e.getMessage());
		} catch (Exception e) {
			throw new MojoExecutionException(e);
		}

		getLog().info("Configured " + artifactId + " (v" + version + ") as Core-Extension!");
		getLog().info("");
	}
}
