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
package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.List;
import java.util.Set;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public interface ProjectExecutionModelCompatProjectParser {

	public ProjectExecutionModel parse(Set<Plugin> invalidPlugins, List<RemoteRepository> repositories,
			RepositorySystemSession session, MavenProject mvnProject) throws Exception;

}
