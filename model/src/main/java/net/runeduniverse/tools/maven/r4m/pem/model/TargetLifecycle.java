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
package net.runeduniverse.tools.maven.r4m.pem.model;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;
import static net.runeduniverse.lib.utils.common.HashUtils.hash;
import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;

public class TargetLifecycle implements DataEntry {

	protected final String id;
	protected String startPhase = null;
	protected String stopPhase = null;

	public TargetLifecycle(final String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public String getStartPhase() {
		return this.startPhase;
	}

	public String getStopPhase() {
		return this.stopPhase;
	}

	public void setStartPhase(final String value) {
		this.startPhase = value;
	}

	public void setStopPhase(final String value) {
		this.stopPhase = value;
	}

	@Override
	public int hashCode() {
		return hash(type()) ^ hash(getId());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof TargetLifecycle))
			return false;
		final TargetLifecycle lifecycle = (TargetLifecycle) obj;

		return objectEquals(this.id, lifecycle.getId()) //
				&& objectEquals(this.startPhase, lifecycle.getStartPhase()) //
				&& objectEquals(this.stopPhase, lifecycle.getStopPhase());
	}

	@Override
	public TargetLifecycle copy() {
		final TargetLifecycle lifecycle = new TargetLifecycle(getId());

		lifecycle.setStartPhase(getStartPhase());
		lifecycle.setStopPhase(getStopPhase());

		return lifecycle;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("Target Lifecycle");

		tree.append("id", this.id);

		if (!isBlank(this.startPhase))
			tree.append("start-phase", this.startPhase);

		if (!isBlank(this.stopPhase))
			tree.append("stop-phase", this.stopPhase);

		return tree;
	}
}
