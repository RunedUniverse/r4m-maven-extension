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
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.grm.parser.api.GoalRequirementModelConfigParser;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSlice;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelConfigParser;
import net.runeduniverse.tools.maven.r4m.scanner.api.MavenProjectScanner;

@Component(role = MavenProjectScanner.class, hint = ConfigProjectScanner.HINT)
public class ConfigProjectScanner implements MavenProjectScanner {

	public static final String HINT = "config";

	@Requirement(role = ProjectExecutionModelConfigParser.class)
	private Map<String, ProjectExecutionModelConfigParser> pemConfigParser;
	@Requirement(role = GoalRequirementModelConfigParser.class)
	private Map<String, GoalRequirementModelConfigParser> grmConfigParser;

	@Override
	public int getPriority() {
		return 1000;
	}

	@Override
	public void scan(MavenSession mvnSession, Collection<Plugin> extPlugins, final Set<Plugin> unidentifiablePlugins,
			MavenProject mvnProject, ExecutionArchiveSlice projectSlice) throws Exception {
		for (ProjectExecutionModelConfigParser parser : this.pemConfigParser.values()) {
			projectSlice.register(parser.parse(mvnProject));
		}
		for (GoalRequirementModelConfigParser parser : this.grmConfigParser.values()) {
			parser.parse(mvnProject);
			// TODO save grm data!
		}
	}
}
