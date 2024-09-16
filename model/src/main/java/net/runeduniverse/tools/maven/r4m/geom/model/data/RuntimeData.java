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
package net.runeduniverse.tools.maven.r4m.geom.model.data;

import java.util.Set;

public interface RuntimeData {

	public String getLifecycleId();

	public String getPhaseId();

	public Set<String> getModes();

	public Set<String> getActiveProfileIds();

	public Set<String> getInactiveProfileIds();

	public Set<String> getMissingProfileIds();

	public Set<String> getProvidedProfileIds();

	public Set<String> getActiveExecutionIds();
}
