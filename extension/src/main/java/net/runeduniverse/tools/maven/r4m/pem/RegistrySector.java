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

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.runeduniverse.tools.maven.r4m.pem.api.PluginExecutionRegistrySector;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ProjectExecutionModel;

public class RegistrySector implements PluginExecutionRegistrySector {

	// save more infos???

	protected final Set<Execution> executions = new LinkedHashSet<>(0);
	protected final Map<Execution, ProjectExecutionModel> executionOrigins = new LinkedHashMap<>(0);
	protected final Map<String, ProjectExecutionModel> hintedOrigins = new LinkedHashMap<>(0);

	@Override
	public Set<Execution> getExecutions() {
		return Collections.unmodifiableSet(this.executions);
	}

	@Override
	public void addExecutions(final Collection<Execution> values) {
		this.executions.addAll(values);
	}

	@Override
	public void includeModel(final ProjectExecutionModel model) {
		if (model == null || model.getExecutions()
				.isEmpty())
			return;

		this.hintedOrigins.put(createKey(model.getParserType(), model.getParserHint()), model);

		for (Execution execution : model.getExecutions()) {
			this.executions.add(execution);
			this.executionOrigins.put(execution, model);
		}
	}

	@Override
	public ProjectExecutionModel getModel(final Class<?> parserType, final String parserHint) {
		return this.hintedOrigins.get(createKey(parserType, parserHint));
	}

	@Override
	public Set<ProjectExecutionModel> getModels() {
		return new LinkedHashSet<>(this.hintedOrigins.values());
	}

	protected static String createKey(final Class<?> parserType, String parserHint) {
		if (isBlank(parserHint))
			parserHint = "default";
		if (parserType == null)
			return parserHint;
		return String.join(":", parserType.getCanonicalName(), parserHint);
	}
}
