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
package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.Recordable;
import net.runeduniverse.tools.maven.r4m.indexer.api.ProjectBoundEntry;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public interface ExecutionArchiveSector extends ProjectBoundEntry<ExecutionArchiveSector>, Recordable {

	public String getVersion();

	public ProjectExecutionModel getModel(Execution execution);

	public Set<Execution> getExecutions();

	public Set<Execution> getExecutions(final ExecutionFilter filter, final boolean onlyInherited);

	public Set<Execution> getEffectiveExecutions(final ExecutionFilter filter, final boolean onlyInherited);

	public void register(ProjectExecutionModel pem);

}
