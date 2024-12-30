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
package net.runeduniverse.tools.maven.r4m.grm.model;

import static net.runeduniverse.tools.maven.r4m.grm.model.ModelUtils.hash;

public class MergeDataGroup extends AndDataGroup {

	private GoalRequirementSource source = null;
	private GoalRequirementCombineMethod combineMethod = null;

	public MergeDataGroup(final String type) {
		super(type);
	}

	public GoalRequirementSource getSource() {
		return this.source;
	}

	public GoalRequirementCombineMethod getCombineMethod() {
		return this.combineMethod;
	}

	public void setSource(final GoalRequirementSource source) {
		this.source = source;
	}

	public void setCombineMethod(final GoalRequirementCombineMethod method) {
		this.combineMethod = method;
	}

	protected <T extends MergeDataGroup> T _copyDataTo(final T group) {
		group.setSource(this.source);
		group.setCombineMethod(this.combineMethod);
		return group;
	}

	@Override
	public MergeDataGroup copy() {
		final MergeDataGroup group = new MergeDataGroup(type());
		_copyDataTo(group);
		return _copyEntriesTo(group);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ hash(getSource()) ^ hash(getCombineMethod());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MergeDataGroup) || !super.equals(obj))
			return false;
		final MergeDataGroup data = (MergeDataGroup) obj;
		return getSource() == data.getSource() && getCombineMethod() == data.getCombineMethod();
	}
}
