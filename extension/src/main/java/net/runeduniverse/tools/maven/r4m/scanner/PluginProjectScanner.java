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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchive;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementModel;
import net.runeduniverse.tools.maven.r4m.grm.parser.api.GoalRequirementModelPluginParser;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelPluginParser;
import net.runeduniverse.tools.maven.r4m.pem.model.DefaultModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.scanner.api.MavenProjectScanner;

@Component(role = MavenProjectScanner.class, hint = PluginProjectScanner.HINT)
public class PluginProjectScanner implements MavenProjectScanner {

	public static final String HINT = "plugin";

	@Requirement(role = ProjectExecutionModelPluginParser.class)
	private Map<String, ProjectExecutionModelPluginParser> pemPluginParser;
	@Requirement(role = GoalRequirementModelPluginParser.class)
	private Map<String, GoalRequirementModelPluginParser> grmPluginParser;
	@Requirement
	private ExecutionArchive pemArchive;
	@Requirement
	private GoalRequirementArchive grmArchive;

	@Override
	public int getPriority() {
		return 0;
	}

	private boolean isValid(final Set<Plugin> invalidPlugins, final Plugin mvnPlugin) {
		if (mvnPlugin == null || invalidPlugins.contains(mvnPlugin))
			return false;

		if (mvnPlugin.getVersion() == null) {
			invalidPlugins.add(mvnPlugin);
			return false;
		}

		return true;
	}

	@Override
	public void scan(final MavenSession mvnSession, final Collection<Plugin> extPlugins,
			final Set<Plugin> invalidPlugins, final MavenProject mvnProject) throws Exception {
		final Path basedir = mvnProject.getBasedir()
				.toPath();
		for (Plugin mvnPlugin : mvnProject.getBuildPlugins()) {
			if (isValid(invalidPlugins, mvnPlugin))
				try {
					for (ProjectExecutionModelPluginParser parser : this.pemPluginParser.values()) {
						final ProjectExecutionModel model = parser.parse(mvnProject.getRemotePluginRepositories(),
								mvnSession.getRepositorySession(), mvnPlugin);
						if (model != null) {
							this.pemArchive.getSector(mvnProject)
									.register(model);

							final ModelSource source = model.computeModelSourceIfAbsent(DefaultModelSource::new);
							if (source.getProjectId() == null)
								source.setProjectId(ModelSource.id(mvnProject::getGroupId, mvnProject::getArtifactId));

							final Path file = source.getFile();
							if (file != null)
								source.setFile(basedir.resolve(file));
						}
					}
					for (GoalRequirementModelPluginParser parser : this.grmPluginParser.values()) {
						final GoalRequirementModel model = parser.parse(mvnProject.getRemotePluginRepositories(),
								mvnSession.getRepositorySession(), mvnPlugin);
						if (model != null) {
							this.grmArchive.getSector(mvnProject)
									.register(model);
						}
					}
				} catch (PluginResolutionException e) {
					invalidPlugins.add(mvnPlugin);
				}
		}
	}
}
