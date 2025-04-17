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
import net.runeduniverse.lib.utils.maven.ext.indexer.AProjectBoundEntry;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSector;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public class DefaultArchiveSector extends AProjectBoundEntry<ExecutionArchiveSector> implements ExecutionArchiveSector {

	protected final Map<String, Map<ExecutionSource, Set<Execution>>> executions = new LinkedHashMap<>();
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
	public Set<Execution> getExecutions(final Predicate<Execution> filter, final boolean onlyInherited) {
		return collectEntries(filter, onlyInherited, false);
	}

	@Override
	public Set<Execution> getEffectiveExecutions(final Predicate<Execution> filter, final boolean onlyInherited) {
		return collectEntries(filter, onlyInherited, true);
	}

	protected Set<Execution> collectEntries(final Predicate<Execution> filter, final boolean onlyInherited,
			final boolean onlyEffective) {
		final Set<Execution> executions = new LinkedHashSet<>();
		for (Map<ExecutionSource, Set<Execution>> entry : this.executions.values()) {
			for (Set<Execution> execCol : entry.values())
				for (Execution execution : execCol) {
					// check for inherited flag
					if (onlyInherited && !execution.isInherited())
						continue;
					// check for user-defined flag
					if (onlyEffective && !this.executionOrigins.get(execution)
							.isEffective())
						continue;
					// apply filter & collect data
					if (filter.test(execution))
						executions.add(execution);
				}
		}
		return executions;
	}

	@Override
	public void register(final ProjectExecutionModel pem) {
		if (pem == null)
			return;

		for (Execution execution : pem.getExecutions()) {
			this.executionOrigins.put(execution, pem);

			final Map<ExecutionSource, Set<Execution>> entry = this.executions.computeIfAbsent(execution.getId(),
					k -> new LinkedHashMap<>(3));
			final Set<Execution> col = entry.computeIfAbsent(execution.getSource(), k -> new HashSet<>());
			col.add(execution);
		}
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = super.toRecord();

		tree.append("version", this.version);

		for (Map<ExecutionSource, Set<Execution>> valuesBySource : this.executions.values())
			for (Set<Execution> executions : valuesBySource.values())
				for (Execution execution : executions)
					tree.append(execution.toRecord());

		return tree;
	}

	@Override
	protected String _getRecordTitle() {
		return "PEM ArchiveSector";
	}
}
