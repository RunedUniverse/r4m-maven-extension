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
package net.runeduniverse.tools.maven.r4m.grm.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;

public class GoalRequirementModel implements Recordable {

	protected final Set<GoalContainer> goals = new LinkedHashSet<>(0);

	protected String version = null;
	protected Class<?> parserType = null;
	protected String parserHint = null;
	protected boolean userDefined = false;
	protected String defaultGroupId = null;
	protected String defaultArtifactId = null;
	protected GoalRequirementSource defaultSource = GoalRequirementSource.PLUGIN;

	public GoalRequirementModel() {
	}

	public String getVersion() {
		return this.version;
	}

	public Class<?> getParserType() {
		return this.parserType;
	}

	public String getParserHint() {
		return this.parserHint;
	}

	// set when grm.xml is found in the project folder
	public boolean isUserDefined() {
		return this.userDefined;
	}

	public String getDefaultGroupId() {
		return this.defaultGroupId;
	}

	public String getDefaultArtifactId() {
		return this.defaultArtifactId;
	}

	public GoalRequirementSource getDefaultSource() {
		return this.defaultSource;
	}

	public Set<GoalContainer> getGoalContainer() {
		return Collections.unmodifiableSet(this.goals);
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public void setParser(final Class<?> type, final String hint) {
		this.parserType = type;
		this.parserHint = hint;
	}

	public void setUserDefined(final boolean value) {
		this.userDefined = value;
	}

	public void setDefaultGroupId(final String defaultGroupId) {
		this.defaultGroupId = defaultGroupId;
	}

	public void setDefaultArtifactId(final String defaultArtifactId) {
		this.defaultArtifactId = defaultArtifactId;
	}

	public void setDefaultSource(final GoalRequirementSource defaultSource) {
		if (defaultSource == null)
			this.defaultSource = GoalRequirementSource.PLUGIN;
		else
			this.defaultSource = defaultSource;
	}

	public void addGoalContainer(final GoalContainer container) {
		this.goals.add(container);
	}

	public void addGoalContainer(final Collection<GoalContainer> container) {
		this.goals.addAll(container);
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("GoalExecutionOrderModel");

		tree.append("userDefined", "" + this.userDefined);

		for (Recordable selector : this.goals)
			tree.append(selector.toRecord());

		return tree;
	}

}
