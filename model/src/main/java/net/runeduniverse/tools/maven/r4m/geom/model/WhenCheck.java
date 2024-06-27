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
package net.runeduniverse.tools.maven.r4m.geom.model;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;

public class WhenCheck extends OrCheck {

	protected boolean always = false;
	protected boolean never = false;

	public WhenCheck() {
	}

	public boolean getAlwaysActive() {
		return this.always;
	}

	public boolean getNeverActive() {
		return this.never;
	}

	public void setAlwaysActive(boolean value) {
		this.always = value;
	}

	public void setNeverActive(boolean value) {
		this.never = value;
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("when");

		tree.append("always", Boolean.toString(this.always));
		tree.append("never", Boolean.toString(this.never));

		return super.toRecord(tree);
	}

}
