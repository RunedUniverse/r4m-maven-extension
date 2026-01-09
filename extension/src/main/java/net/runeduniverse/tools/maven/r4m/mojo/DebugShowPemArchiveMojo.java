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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSector;

import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.acquireExecutionArchive;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.mojoFailureExtensionLoading;

/**
 * Prints all registered PEMs
 *
 * @goal debug-show-pem-archive
 * @requiresProject false
 * @requiresReports false
 * @threadSafe true
 * @since 1.1.0
 * @author VenaNocta
 */
public class DebugShowPemArchiveMojo extends AbstractMojo {

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
	private ExecutionArchive archive;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		ExecutionArchiveSector projectSector = null;
		if (this.archive != null)
			projectSector = this.archive.getSector(this.mvnProject);

		if (projectSector == null) {
			// try loading via build-extension classrealm
			this.archive = acquireExecutionArchive(mvnSession, (ClassRealm) Thread.currentThread()
					.getContextClassLoader());
		}
		if (this.archive != null)
			projectSector = this.archive.getSector(this.mvnProject);

		if (projectSector == null)
			mojoFailureExtensionLoading(getLog());

		getLog().info("");
		getLog().info("PEM Archive Log");
		getLog().info("");
		final String log = this.archive.toRecord()
				.toString();
		for (String line : log.split("\n")) {
			getLog().info(line);
		}
		getLog().info("");
	}
}
