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
package net.runeduniverse.tools.maven.r4m.scanner.api;

import java.util.Collection;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

/**
 * Required for scanning maven projects to extract pem configurations.
 */
public interface MavenProjectScanner extends Comparable<MavenProjectScanner> {

	@Override
	public default int compareTo(MavenProjectScanner o) {
		if (this == o)
			return 0;
		if (o == null)
			return 1;
		return Integer.compare(getPriority(), o.getPriority());
	}

	public int getPriority();

	public void scan(final MavenSession mvnSession, final Collection<Plugin> extPlugins,
			final Set<Plugin> invalidPlugins, final MavenProject mvnProject) throws Exception;

}
