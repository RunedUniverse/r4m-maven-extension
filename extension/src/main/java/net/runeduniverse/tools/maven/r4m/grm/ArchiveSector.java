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
package net.runeduniverse.tools.maven.r4m.grm;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.grm.api.GoalRequirementArchiveSector;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementModel;
import net.runeduniverse.tools.maven.r4m.indexer.AProjectBoundEntry;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSector;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionFilter;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public class ArchiveSector extends AProjectBoundEntry<GoalRequirementArchiveSector>
		implements GoalRequirementArchiveSector {

	public ArchiveSector(final MavenProject mvnProject, final ArchiveSector parent) {
		super(mvnProject, parent);
	}

	// TODO implement getModel()
	// @Override
	// public GoalRequirementModel getModel(Object sourcedModelEntry) {
	// }

	@Override
	public void register(final GoalRequirementModel grm) {
		if (grm == null)
			return;

		// TODO index model
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = super.toRecord();

		// TODO log entries
		/*
		 * for (Map<ExecutionSource, Set<Execution>> valuesBySource :
		 * this.executions.values()) for (Set<Execution> executions :
		 * valuesBySource.values()) for (Execution execution : executions)
		 * tree.append(execution.toRecord());
		 */

		return tree;
	}

	@Override
	protected String _getRecordTitle() {
		return "ArchiveSector";
	}
}
