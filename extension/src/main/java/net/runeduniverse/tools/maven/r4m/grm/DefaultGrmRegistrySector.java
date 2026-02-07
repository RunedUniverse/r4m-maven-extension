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
package net.runeduniverse.tools.maven.r4m.grm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.runeduniverse.tools.maven.r4m.grm.api.PluginRequirementRegistrySector;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalContainer;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementModel;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

public class DefaultGrmRegistrySector implements PluginRequirementRegistrySector {

	// save more infos???

	protected final Set<GoalContainer> containerSet = new LinkedHashSet<>(0);
	protected final Map<GoalContainer, GoalRequirementModel> containerOrigins = new LinkedHashMap<>(0);
	protected final Map<String, GoalRequirementModel> hintedOrigins = new LinkedHashMap<>(0);

	@Override
	public Set<GoalContainer> getGoalContainer() {
		return Collections.unmodifiableSet(this.containerSet);
	}

	@Override
	public void addGoalContainer(final Collection<GoalContainer> values) {
		this.containerSet.addAll(values);
	}

	@Override
	public void includeModel(final GoalRequirementModel model) {
		if (model == null || model.getGoalContainer()
				.isEmpty())
			return;

		this.hintedOrigins.put(createKey(model.getParserType(), model.getParserHint()), model);

		for (GoalContainer container : model.getGoalContainer()) {
			this.containerSet.add(container);
			this.containerOrigins.put(container, model);
		}
	}

	@Override
	public GoalRequirementModel getModel(final Class<?> parserType, final String parserHint) {
		return this.hintedOrigins.get(createKey(parserType, parserHint));
	}

	@Override
	public Set<GoalRequirementModel> getModels() {
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
