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
package net.runeduniverse.tools.maven.r4m.scanner;

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

	private boolean isIdentifiable(final Set<Plugin> unidentifiablePlugins, Plugin mvnPlugin) {
		if (mvnPlugin == null || unidentifiablePlugins.contains(mvnPlugin))
			return false;

		if (mvnPlugin.getVersion() == null) {
			unidentifiablePlugins.add(mvnPlugin);
			return false;
		}

		return true;
	}

	@Override
	public void scan(MavenSession mvnSession, Collection<Plugin> extPlugins, final Set<Plugin> unidentifiablePlugins,
			MavenProject mvnProject) throws Exception {
		for (Plugin mvnPlugin : mvnProject.getBuildPlugins()) {
			if (isIdentifiable(unidentifiablePlugins, mvnPlugin))
				try {
					for (ProjectExecutionModelPluginParser parser : this.pemPluginParser.values()) {
						final ProjectExecutionModel model = parser.parse(mvnProject.getRemotePluginRepositories(),
								mvnSession.getRepositorySession(), mvnPlugin);
						this.pemArchive.getSector(mvnProject)
								.register(model);
					}
					for (GoalRequirementModelPluginParser parser : this.grmPluginParser.values()) {
						final GoalRequirementModel model = parser.parse(mvnProject.getRemotePluginRepositories(),
								mvnSession.getRepositorySession(), mvnPlugin);
						this.grmArchive.getSector(mvnProject)
								.register(model);
					}
				} catch (PluginResolutionException e) {
					unidentifiablePlugins.add(mvnPlugin);
				}
		}
	}
}
