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
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.configuration.DefaultPlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.grm.converter.api.AbstractCheckDataFactory;
import net.runeduniverse.tools.maven.r4m.grm.converter.api.CheckDataFactory;
import net.runeduniverse.tools.maven.r4m.grm.model.AndDataGroup;
import net.runeduniverse.tools.maven.r4m.grm.model.ArtifactIdData;
import net.runeduniverse.tools.maven.r4m.grm.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.grm.model.DataGroup;
import net.runeduniverse.tools.maven.r4m.grm.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalContainer;
import net.runeduniverse.tools.maven.r4m.grm.model.GoalIdData;
import net.runeduniverse.tools.maven.r4m.grm.model.GroupIdData;
import net.runeduniverse.tools.maven.r4m.grm.model.MergeDataGroup;
import net.runeduniverse.tools.maven.r4m.grm.model.OrDataGroup;

@Component(role = AbstractCheckDataFactory.class, hint = "default")
public class DefaultAbstractCheckDataFactory implements AbstractCheckDataFactory {

	@Requirement(role = CheckDataFactory.class)
	protected Map<String, CheckDataFactory> factories;

	public GoalContainer createContainer(final PlexusConfiguration cnf, final String defaultGroupId,
			final String defaultArtifactId, final ExecutionSource defaultSource) {
		final PlexusConfiguration preqCnf = cnf.getChild("prerequisites", true);
		final PlexusConfiguration depCnf = cnf.getChild("dependents", true);

		final DataGroup match = collectMatchData(copy(cnf, Collections.emptyList(), Arrays.asList( //
				"prerequisites", "dependents" //
		)), defaultGroupId, defaultArtifactId);

		final GoalContainer containerData = new GoalContainer(match, new OrDataGroup(), new OrDataGroup());

		for (PlexusConfiguration entry : preqCnf.getChildren()) {
			containerData.addPrerequisiteEntry(collectRequirementData(entry, defaultSource));
		}
		for (PlexusConfiguration entry : depCnf.getChildren()) {
			containerData.addDependentEntry(collectRequirementData(entry, defaultSource));
		}

		return containerData;
	}

	public DataEntry createEntry(final PlexusConfiguration cnf) {
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

	protected DataGroup collectMatchData(PlexusConfiguration cnf, final String defaultGroupId,
			final String defaultArtifactId) {
		final AndDataGroup group = new AndDataGroup();
		final PlexusConfiguration groupIdCnf = cnf.getChild("groupId", true);
		final PlexusConfiguration artifactIdCnf = cnf.getChild("artifactId", true);

		group.addEntry(new GoalIdData().setGoalId(cnf.getAttribute("id")));
		group.addEntry(new GroupIdData().setGroupId(groupIdCnf.getValue(defaultGroupId)));
		group.addEntry(new ArtifactIdData().setArtifactId(artifactIdCnf.getValue(defaultArtifactId)));

		for (PlexusConfiguration child : cnf.getChildren()) {
			if (child == groupIdCnf || child == artifactIdCnf)
				continue;
			final DataEntry entry = createEntry(child);
			if (entry == null)
				continue;
			group.addEntry(entry);
		}

		return group;
	}

	protected DataGroup collectRequirementData(PlexusConfiguration cnf, final ExecutionSource defaultSource) {
		final MergeDataGroup group = new MergeDataGroup();
		final PlexusConfiguration groupIdCnf = cnf.getChild("groupId", true);
		final PlexusConfiguration artifactIdCnf = cnf.getChild("artifactId", true);

		group.setSource(ExecutionSource.create(cnf.getAttribute("source", defaultSource.key())));
		group.addEntry(new GoalIdData().setGoalId(cnf.getAttribute("id")));
		group.addEntry(new GroupIdData().setGroupId(groupIdCnf.getValue("org.apache.maven.plugins")));
		group.addEntry(new ArtifactIdData().setArtifactId(artifactIdCnf.getValue()));

		for (PlexusConfiguration child : cnf.getChildren()) {
			if (child == groupIdCnf || child == artifactIdCnf)
				continue;
			final DataEntry entry = createEntry(child);
			if (entry == null)
				continue;
			group.addEntry(entry);
		}

		return group;
	}
}
