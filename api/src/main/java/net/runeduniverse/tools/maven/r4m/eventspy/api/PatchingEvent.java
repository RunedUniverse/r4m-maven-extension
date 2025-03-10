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
package net.runeduniverse.tools.maven.r4m.eventspy.api;

public interface PatchingEvent {

	public enum Type {
		INFO_PATCHING_STARTED, INFO_PATCHING_FINISHED, INFO_PATCHING_ABORTED, //
		INFO_ELEVATING_TO_PLEXUS_REALM, INFO_ELEVATING_TO_CORE_REALM, //
		INFO_ELEVATING_TO_BUILD_REALM, INFO_RETURNING_TO_EXTENSION_REALM, //
		INFO_EXTENSIONS_DETECTED, //
		WARN_INVALID_PLUGIN_DETECTED, //
		INFO_LIFECYCLE_EXEC_PLAN_CALC_STARTED, INFO_LIFECYCLE_EXEC_PLAN_CALC_FINISHED,
		WARN_LIFECYCLE_EXEC_PLAN_CALC_FAILED_TO_LOCATE_PLEXUS_COMPONENT,
		WARN_LIFECYCLE_EXEC_PLAN_CALC_FAILED_TO_RELEASE_PLEXUS_COMPONENT,
		DEBUG_LIFECYCLE_EXEC_PLAN_CALC_UPDATING_PLEXUS_COMPONENT_DESCRIPTOR, //
		INFO_SCANNING_FOR_REFERENCED_PLUGINS_STARTED, INFO_SCANNING_FOR_REFERENCED_PLUGINS_FINISHED,
		INFO_SCANNING_FOR_REFERENCED_PLUGINS_BY_PROJECT_STARTED,
		INFO_SCANNING_FOR_REFERENCED_PLUGINS_BY_PROJECT_FINISHED, //
	}

	public Type getType();

	public Exception getException();

	public static BasicEvent createInfoEvent(Type type) {
		return new BasicEvent(type, null);
	}

	public static BasicEvent createErrorEvent(Type type, Exception exception) {
		return new BasicEvent(type, exception);
	}

	public class BasicEvent implements PatchingEvent {

		protected Type type = null;
		protected Exception exception = null;

		public BasicEvent(Type type, Exception exception) {
			this.type = type;
			this.exception = exception;
		}

		@Override
		public Type getType() {
			return this.type;
		}

		@Override
		public Exception getException() {
			return this.exception;
		}

	}

}
