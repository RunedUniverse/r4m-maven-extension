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
package net.runeduniverse.tools.maven.r4m.pem.model;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;

import static net.runeduniverse.lib.utils.common.HashUtils.hash;
import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;
import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

public class DisableProjectPomOverride implements ModelOverride {

	public static final String TYPE = "no-project-pom";
	public static final String HINT = "disable-project-pom";
	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.pem.model.DisableProjectPomOverride";

	protected String groupId;
	protected String artifactId;
	protected boolean active;

	public DisableProjectPomOverride(final String groupId, final String artifactId, final boolean active) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.active = active;
	}

	@Override
	public String type() {
		return TYPE;
	}

	@Override
	public String hint() {
		return HINT;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	@Override
	public boolean isActive() {
		return this.active;
	}

	public void setGroupId(final String groupId) {
		this.groupId = groupId;
	}

	public void setArtifactId(final String artifactId) {
		this.artifactId = artifactId;
	}

	@Override
	public void setActive(final boolean active) {
		this.active = active;
	}

	@Override
	public boolean validate() {
		return !(isBlank(getGroupId()) || isBlank(getArtifactId()));
	}

	@Override
	public int hashCode() {
		return hash(HINT);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DisableProjectPomOverride))
			return false;
		final DisableProjectPomOverride override = (DisableProjectPomOverride) obj;

		return objectEquals(type(), override.type()) && //
				objectEquals(this.groupId, override.getGroupId()) && //
				objectEquals(this.artifactId, override.getArtifactId()) && //
				objectEquals(this.active, override.isActive());
	}

	@Override
	public DisableProjectPomOverride copy() {
		final DisableProjectPomOverride override = new DisableProjectPomOverride( //
				getGroupId(), getArtifactId(), isActive());
		return override;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree(HINT);
		tree.append("groupId", this.groupId);
		tree.append("artifactId", this.artifactId);
		tree.append("active", Boolean.toString(this.active));
		return tree;
	}
}
