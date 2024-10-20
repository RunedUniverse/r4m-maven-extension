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

import net.runeduniverse.lib.utils.conditions.api.Condition;
import net.runeduniverse.tools.maven.r4m.grm.model.data.EntityData;
import net.runeduniverse.tools.maven.r4m.grm.model.data.GoalData;
import net.runeduniverse.tools.maven.r4m.grm.model.data.ProjectData;
import net.runeduniverse.tools.maven.r4m.grm.model.data.RuntimeData;

public abstract class DefaultCheck implements Condition<EntityData> {

	@Override
	public boolean evaluate(EntityData entity) {
		return and(this::valuesAreValid, check()).eval(entity);
	}

	protected boolean valuesAreValid(EntityData data) {
		return isValid();
	}

	protected DataCheck<EntityData> check() {
		return d -> false;
	}

	protected static <T> DataCheck<T> isNull() {
		return d -> d == null;
	}

	protected static <T> DataCheck<T> nonNull() {
		return d -> d != null;
	}

	protected static DataCheck<EntityData> project(DataCheck<ProjectData> check) {
		return d -> check.eval(d.getProject());
	}

	protected static DataCheck<EntityData> runtime(DataCheck<RuntimeData> check) {
		return d -> check.eval(d.getRuntime());
	}

	protected static DataCheck<EntityData> goal(DataCheck<GoalData> check) {
		return d -> check.eval(d.getGoal());
	}

	@SafeVarargs
	protected static <T> DataCheck<T> and(DataCheck<T>... checks) {
		return new DataCheck<T>() {
			@Override
			public boolean eval(T data) {
				for (DataCheck<T> check : checks) {
					if (!check.eval(data))
						return false;
				}
				return true;
			}
		};
	}

	@SafeVarargs
	protected static <T> DataCheck<T> or(DataCheck<T>... checks) {
		return new DataCheck<T>() {
			@Override
			public boolean eval(T data) {
				for (DataCheck<T> check : checks) {
					if (check.eval(data))
						return true;
				}
				return false;
			}
		};
	}

	@FunctionalInterface
	public interface DataCheck<T> {

		public boolean eval(T data);
	}
}
