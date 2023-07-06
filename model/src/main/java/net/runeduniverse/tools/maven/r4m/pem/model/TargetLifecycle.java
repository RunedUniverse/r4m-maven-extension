/*
 * Copyright © 2023 VenaNocta (venanocta@gmail.com)
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

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.lib.utils.logging.logs.Recordable;

public class TargetLifecycle implements Recordable {

	private String id;
	private String startPhase = null;
	private String stopPhase = null;

	public TargetLifecycle(String id) {
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

	public void setStartPhase(String value) {
		this.startPhase = value;
	}

	public void setStopPhase(String value) {
		this.stopPhase = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof TargetLifecycle))
			return false;
		TargetLifecycle lifecycle = (TargetLifecycle) obj;

		if (!this.id.equals(lifecycle.getId()))
			return false;

		if (this.startPhase == null) {
			if (lifecycle.getStartPhase() != null)
				return false;
		} else if (!this.startPhase.equals(lifecycle.getStartPhase()))
			return false;

		if (this.stopPhase == null) {
			if (lifecycle.getStopPhase() != null)
				return false;
		} else if (!this.stopPhase.equals(lifecycle.getStopPhase()))
			return false;

		return true;
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("Target Lifecycle");

		tree.append("id", this.id);

		if (!isBlank(this.startPhase))
			tree.append("start-phase", this.startPhase);

		if (!isBlank(this.stopPhase))
			tree.append("stop-phase", this.stopPhase);

		return tree;
	}

}
