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
package net.runeduniverse.tools.maven.r4m.grm.converter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.DefaultPlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.grm.converter.api.CheckDataConverter;
import net.runeduniverse.tools.maven.r4m.grm.converter.api.CheckDataFactory;
import net.runeduniverse.tools.maven.r4m.grm.converter.api.CheckDataHandler;
import net.runeduniverse.tools.maven.r4m.grm.converter.api.ConfigurationFactory;
import net.runeduniverse.tools.maven.r4m.grm.model.AndDataGroup;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.DataGroup;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementSource;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalContainer;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalData;
import net.runeduniverse.tools.maven.r4m.grm.model.MergeDataGroup;
import net.runeduniverse.tools.maven.r4m.grm.model.OrDataGroup;

@Component(role = CheckDataConverter.class, hint = "default")
public class DefaultCheckDataConverter implements CheckDataConverter {

	public static final String CNF_MATCH_BEFORE_TAG = "prerequisites";
	public static final String CNF_MATCH_AFTER_TAG = "dependents";

	public static int MAX_TYPE_SEARCH_DEPTH = 4;

	@Requirement(role = CheckDataFactory.class)
	protected Map<String, CheckDataFactory> factories;
	@Requirement(role = CheckDataHandler.class)
	protected Map<String, CheckDataHandler> handler;

	@Override
	public GoalContainer convertContainer(final PlexusConfiguration cnf, final String defaultGroupId,
			final String defaultArtifactId, final GoalRequirementSource defaultSource) {
		if (cnf == null)
			return null;

		final PlexusConfiguration preqCnf = cnf.getChild(CNF_MATCH_BEFORE_TAG, true);
		final PlexusConfiguration depCnf = cnf.getChild(CNF_MATCH_AFTER_TAG, true);

		final DataGroup match = collectMatchData(copy(cnf, Collections.emptyList(), Arrays.asList( //
				CNF_MATCH_BEFORE_TAG, CNF_MATCH_AFTER_TAG //
		)), defaultGroupId, defaultArtifactId);

		final GoalContainer containerData = new GoalContainer(match, new OrDataGroup(), new OrDataGroup());

		for (PlexusConfiguration entry : preqCnf.getChildren()) {
			containerData.addPrerequisiteEntry(collectRequirementData(CNF_MATCH_BEFORE_TAG, entry, defaultGroupId,
					defaultArtifactId, defaultSource));
		}
		for (PlexusConfiguration entry : depCnf.getChildren()) {
			containerData.addDependentEntry(collectRequirementData(CNF_MATCH_AFTER_TAG, entry, defaultGroupId,
					defaultArtifactId, defaultSource));
		}

		return containerData;
	}

	@Override
	public DataEntry convertEntry(final PlexusConfiguration cnf) {
		if (cnf == null)
			return null;
		final CheckDataFactory factory = this.factories.get(cnf.getName());
		if (factory == null)
			return null;
		return factory.createEntry(cnf);
	}

	protected PlexusConfiguration copy(PlexusConfiguration cnf, Collection<String> excludeAttributes,
			Collection<String> excludeChildren) {
		final PlexusConfiguration copy = new DefaultPlexusConfiguration(cnf.getName(), cnf.getValue());

		for (String name : cnf.getAttributeNames()) {
			if (!excludeAttributes.contains(name))
				copy.setAttribute(name, cnf.getAttribute(name));
		}

		for (PlexusConfiguration child : cnf.getChildren()) {
			if (!excludeChildren.contains(child.getName()))
				copy.addChild(child);
		}
		return copy;
	}

	protected void collectGoalData(final PlexusConfiguration cnf, final String defaultGroupId,
			final String defaultArtifactId, final DataGroup group) {
		final PlexusConfiguration groupIdCnf = cnf.getChild("groupId", true);
		final PlexusConfiguration artifactIdCnf = cnf.getChild("artifactId", true);

		group.addEntry(new GoalData() //
				.setGroupId(groupIdCnf.getValue(defaultGroupId))
				.setArtifactId(artifactIdCnf.getValue(defaultArtifactId))
				.setGoalId(cnf.getAttribute("id")));

		for (PlexusConfiguration child : cnf.getChildren()) {
			if (child == groupIdCnf || child == artifactIdCnf)
				continue;
			final DataEntry entry = convertEntry(child);
			if (entry == null)
				continue;
			group.addEntry(entry);
		}
	}

	protected DataGroup collectMatchData(final PlexusConfiguration cnf, final String defaultGroupId,
			final String defaultArtifactId) {
		final AndDataGroup group = new AndDataGroup() {
			@Override
			public String type() {
				return "match";
			}
		};
		collectGoalData(cnf, defaultGroupId, defaultArtifactId, group);
		return group;
	}

	protected DataGroup collectRequirementData(final String type, final PlexusConfiguration cnf,
			final String defaultGroupId, final String defaultArtifactId, final GoalRequirementSource defaultSource) {
		final MergeDataGroup group = new MergeDataGroup(type);
		collectGoalData(cnf, defaultGroupId, defaultArtifactId, group);
		group.setSource(GoalRequirementSource.create(cnf.getAttribute("source", defaultSource.key())));
		return group;
	}

	@Override
	public PlexusConfiguration convertContainer(final ConfigurationFactory<PlexusConfiguration> factory,
			final GoalContainer container) {
		if (factory == null || container == null)
			return null;
		final PlexusConfiguration goalCnf = convertGoalData(factory, container.getMatchGroup());
		if (goalCnf == null)
			return null;

		processRequirementData(factory, container.getPrerequisiteEntries(), //
				goalCnf.getChild(CNF_MATCH_BEFORE_TAG, true));
		processRequirementData(factory, container.getDependentEntries(), //
				goalCnf.getChild(CNF_MATCH_AFTER_TAG, true));

		return goalCnf;
	}

	protected void processRequirementData(final ConfigurationFactory<PlexusConfiguration> factory,
			final Collection<DataEntry> entries, final PlexusConfiguration targetCnf) {
		for (DataEntry entry : entries) {
			PlexusConfiguration cnf = null;
			if (entry instanceof MergeDataGroup) {
				cnf = convertGoalData(factory, (MergeDataGroup) entry);
			}
			if (cnf == null)
				continue;
			targetCnf.addChild(cnf);
		}
	}

	@Override
	public PlexusConfiguration convertEntry(final ConfigurationFactory<PlexusConfiguration> factory,
			final DataEntry entry) {
		if (factory == null || entry == null)
			return null;

		final List<String> types = collectEntryTypes(entry.getClass());
		PlexusConfiguration cnf = null;
		CheckDataHandler handler = null;

		for (String type : types) {
			// find valid handler
			handler = this.handler.get(type);
			if (handler == null)
				continue;
			// check if the handler rejects the entry
			cnf = handler.createConfiguration(factory, entry);
			if (cnf != null)
				return cnf;
		}
		return null;
	}

	protected List<String> collectEntryTypes(Class<?> clazz) {
		final List<String> lst = new LinkedList<>();
		// add class
		lst.add(clazz.getCanonicalName());

		for (int i = 0; i < MAX_TYPE_SEARCH_DEPTH; i++) {
			if (clazz == Object.class)
				return lst;
			// add all interfaces
			for (Class<?> ic : clazz.getInterfaces()) {
				lst.add(ic.getCanonicalName());
			}
			// add superclass
			clazz = clazz.getSuperclass();
			lst.add(clazz.getCanonicalName());
		}
		return lst;
	}

	protected PlexusConfiguration convertGoalData(final ConfigurationFactory<PlexusConfiguration> factory,
			final DataGroup goalData) {
		if (goalData == null)
			return null;
		final PlexusConfiguration goalCnf = factory.create("goal");

		if (goalData instanceof MergeDataGroup) {
			final GoalRequirementSource source = ((MergeDataGroup) goalData).getSource();
			if (source != null) {
				goalCnf.setAttribute("source", source.key());
			}
		}

		for (DataEntry entry : goalData.getEntries()) {
			if (entry == null)
				continue;
			// check for basic values
			if (entry instanceof GoalData) {
				final GoalData data = (GoalData) entry;
				goalCnf.addChild("groupId", data.getGroupId());
				goalCnf.addChild("artifactId", data.getArtifactId());
				goalCnf.setAttribute("id", data.getGoalId());
				break;
			}
			// check additional values > by default only "when" is expected
			final PlexusConfiguration childCnf = convertEntry(factory, entry);
			if (childCnf != null)
				goalCnf.addChild(childCnf);
		}
		return goalCnf;
	}
}
