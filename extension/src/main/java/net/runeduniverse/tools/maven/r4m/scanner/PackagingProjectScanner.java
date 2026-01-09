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
package net.runeduniverse.tools.maven.r4m.scanner;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchive;
import net.runeduniverse.tools.maven.r4m.pem.api.ProjectExecutionModelPackagingParser;
import net.runeduniverse.tools.maven.r4m.pem.model.DefaultModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ModelSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;
import net.runeduniverse.tools.maven.r4m.scanner.api.MavenProjectScanner;

@Component(role = MavenProjectScanner.class, hint = PackagingProjectScanner.HINT)
public class PackagingProjectScanner implements MavenProjectScanner {

	public static final String HINT = "packaging";

	@Requirement(role = ProjectExecutionModelPackagingParser.class)
	private Map<String, ProjectExecutionModelPackagingParser> pemPackagingParser;
	@Requirement
	private ExecutionArchive pemArchive;

	@Override
	public int getPriority() {
		return -1000;
	}

	@Override
	public void scan(final MavenSession mvnSession, final Collection<Plugin> extPlugins,
			final Set<Plugin> invalidPlugins, final MavenProject mvnProject) throws Exception {
		for (ProjectExecutionModelPackagingParser parser : this.pemPackagingParser.values()) {
			final ProjectExecutionModel model = parser.parse();
			if (model == null)
				continue;
			this.pemArchive.getSector(mvnProject)
					.register(model);

			final ModelSource source = model.computeModelSourceIfAbsent(DefaultModelSource::new);
			if (source.getProjectId() == null)
				source.setProjectId(ModelSource.id(mvnProject::getGroupId, mvnProject::getArtifactId));
		}
	}
}
