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
package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.maven3.ext.indexer.AProjectBoundEntry;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSector;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSectorSnapshot;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public class DefaultArchiveSector extends AProjectBoundEntry<ExecutionArchiveSector> implements ExecutionArchiveSector {

	protected final Map<ProjectExecutionModel, Set<Execution>> models = new LinkedHashMap<>();
	protected final Map<String, Set<Execution>> executions = new LinkedHashMap<>();
	protected final Map<Execution, ProjectExecutionModel> executionOrigins = new LinkedHashMap<>();

	protected final String version;

	public DefaultArchiveSector(final MavenProject mvnProject, final String version,
			final DefaultArchiveSector parent) {
		super(mvnProject, parent);
		this.version = version;
	}

	@Override
	public String getVersion() {
		return this.version;
	}

	@Override
	public ProjectExecutionModel getModel(final Execution execution) {
		return this.executionOrigins.get(execution);
	}

	@Override
	public Set<Execution> getExecutions() {
		return Collections.unmodifiableSet(this.executionOrigins.keySet());
	}

	@Override
	public Set<Execution> getExecutions(final String id) {
		return Collections.unmodifiableSet(this.executions.getOrDefault(id, Collections.emptySet()));
	}

	@Override
	public void register(final ProjectExecutionModel pem) {
		if (pem == null)
			return;

		final Set<Execution> perModelSet = this.models.computeIfAbsent(pem, k -> new LinkedHashSet<>(1));
		for (Execution execution : pem.getExecutions()) {
			if (execution == null)
				continue;
			perModelSet.add(execution);
			this.executionOrigins.put(execution, pem);

			final Set<Execution> col = this.executions.computeIfAbsent(execution.getId(), k -> new HashSet<>());
			col.add(execution);
		}
	}

	@Override
	public ExecutionArchiveSectorSnapshot snapshot() {
		final ExecutionArchiveSectorSnapshot snapshot = new DefaultSectorSnapshot(this.mvnProject,
				this.parent == null ? null : this.parent.snapshot());
		for (ProjectExecutionModel pem : this.models.keySet()) {
			snapshot.addModel(pem);
		}
		return snapshot;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = super.toRecord();

		tree.append("version", this.version);

		for (Set<Execution> executions : this.executions.values())
			for (Execution execution : executions)
				tree.append(execution.toRecord());

		return tree;
	}

	@Override
	protected String _getRecordTitle() {
		return "PEM ArchiveSector";
	}
}
