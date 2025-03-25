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
package net.runeduniverse.tools.maven.r4m.grm.check;

import java.util.LinkedHashSet;

import net.runeduniverse.lib.utils.conditional.api.Condition;
import net.runeduniverse.tools.maven.r4m.grm.view.api.EntityView;

public class MatchCheck extends AndCheck {

	private int priority = 0;

	public MatchCheck(final String matchType) {
		super(matchType, new UniqueGoalCheckLinkedHashSet());
	}

	public MatchCheck(final String matchType, final UniqueGoalCheckLinkedHashSet conditions) {
		super(matchType, conditions);
	}

	public GoalCheck getGoalCheck() {
		return ((UniqueGoalCheckLinkedHashSet) this.conditions).getGoalCheck();
	}

	public boolean setGoalCheck(final GoalCheck goalCheck) {
		return ((UniqueGoalCheckLinkedHashSet) this.conditions).setGoalCheck(goalCheck);
	}

	@Override
	public int getPriority() {
		return this.priority;
	}

	public void setPriority(final int priority) {
		this.priority = priority;
	}

	@Override
	public boolean isValid() {
		if (getGoalCheck() == null)
			return false;
		return super.isValid();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MatchCheck))
			return false;
		return super.equals(obj);
	}

	public static class UniqueGoalCheckLinkedHashSet extends LinkedHashSet<Condition<EntityView>> {

		private static final long serialVersionUID = 1L;

		private GoalCheck goalCheck = null;

		public GoalCheck getGoalCheck() {
			return goalCheck;
		}

		public boolean setGoalCheck(final GoalCheck goalCheck) {
			removeGoalCheck();
			return add(goalCheck);
		}

		@Override
		public boolean add(final Condition<EntityView> e) {
			if (e instanceof GoalCheck) {
				if (this.goalCheck != null)
					return false;
				this.goalCheck = (GoalCheck) e;
			}
			return super.add(e);
		}

		@Override
		public boolean remove(final Object o) {
			final boolean result = super.remove(o);
			if (result && o instanceof GoalCheck) {
				this.goalCheck = null;
			}
			return result;
		}

		@Override
		public void clear() {
			super.clear();
			this.goalCheck = null;
		}

		public void removeGoalCheck() {
			remove(this.goalCheck);
		}
	}
}
