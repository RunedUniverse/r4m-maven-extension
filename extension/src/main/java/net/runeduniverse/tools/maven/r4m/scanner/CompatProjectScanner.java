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
package net.runeduniverse.tools.maven.r4m.scanner;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelCompatProjectParser;
import net.runeduniverse.tools.maven.r4m.pem.model.DefaultModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.scanner.api.MavenProjectScanner;

@Component(role = MavenProjectScanner.class, hint = CompatProjectScanner.HINT)
public class CompatProjectScanner implements MavenProjectScanner {

	public static final String HINT = "compat";

	@Requirement
	protected Logger log;
	@Requirement(role = ProjectExecutionModelCompatProjectParser.class)
	private Map<String, ProjectExecutionModelCompatProjectParser> pemParser;
	@Requirement
	private ExecutionArchive pemArchive;
	@Requirement
	private GoalRequirementArchive grmArchive;

	@Override
	public int getPriority() {
		return 10000;
	}

	@Override
	public void scan(final MavenSession mvnSession, final Collection<Plugin> extPlugins,
			final Set<Plugin> invalidPlugins, final MavenProject mvnProject) throws Exception {
		final File origBasedir = mvnProject.getBasedir();
		final Path basedir = origBasedir == null ? null : origBasedir.toPath();

		for (ProjectExecutionModelCompatProjectParser parser : this.pemParser.values()) {
			final ProjectExecutionModel model = parser.parse(invalidPlugins, mvnProject.getRemotePluginRepositories(),
					mvnSession.getRepositorySession(), mvnProject);
			if (model != null) {
				this.pemArchive.getSector(mvnProject)
						.register(model);

				final ModelSource source = model.computeModelSourceIfAbsent(DefaultModelSource::new);
				if (source.getProjectId() == null)
					source.setProjectId(ModelSource.id(mvnProject::getGroupId, mvnProject::getArtifactId));

				final Path file = source.getFile();
				if (file != null)
					source.setFile(basedir == null ? file : basedir.resolve(file));
			}
		}
	}
}
