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
package net.runeduniverse.tools.maven.r4m.grm.view;

import java.util.LinkedHashSet;
import java.util.Set;

import net.runeduniverse.tools.maven.r4m.grm.view.api.RuntimeView;

public class DefaultRuntimeView implements RuntimeView {

	protected final Set<String> modes = new LinkedHashSet<>();
	protected final Set<String> activeProfileIds = new LinkedHashSet<>();
	protected final Set<String> inactiveProfileIds = new LinkedHashSet<>();
	protected final Set<String> providedProfileIds = new LinkedHashSet<>();
	protected final Set<String> activeExecutionIds = new LinkedHashSet<>();

	protected String lifecycleId;
	protected String phaseId;

	protected DefaultRuntimeView() {
	}

	public DefaultRuntimeView(final String lifecycleId, final String phaseId, final Set<String> modes,
			final Set<String> activeProfileIds, final Set<String> inactiveProfileIds,
			final Set<String> providedProfileIds, final Set<String> activeExecutionIds) {
		this.lifecycleId = lifecycleId;
		this.phaseId = phaseId;
		this.modes.addAll(modes);
		this.activeProfileIds.addAll(activeProfileIds);
		this.inactiveProfileIds.addAll(inactiveProfileIds);
		this.providedProfileIds.addAll(providedProfileIds);
		this.activeExecutionIds.addAll(activeExecutionIds);
	}

	@Override
	public String getLifecycleId() {
		return this.lifecycleId;
	}

	@Override
	public String getPhaseId() {
		return this.phaseId;
	}

	@Override
	public Set<String> getModes() {
		return this.modes;
	}

	@Override
	public Set<String> getActiveProfileIds() {
		return this.activeProfileIds;
	}

	@Override
	public Set<String> getInactiveProfileIds() {
		return this.inactiveProfileIds;
	}

	@Override
	public Set<String> getProvidedProfileIds() {
		return this.providedProfileIds;
	}

	@Override
	public Set<String> getActiveExecutionIds() {
		return this.activeExecutionIds;
	}
}
