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
package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.util.List;

import org.apache.maven.lifecycle.Lifecycle;

import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskRequest;

public class DefaultLifecycleTaskRequest implements LifecycleTaskRequest {

	private final Lifecycle lifecycle;
	private final List<String> phaseSequence;

	public DefaultLifecycleTaskRequest(final Lifecycle lifecycle, final List<String> phaseSequence) {
		this.lifecycle = lifecycle;
		this.phaseSequence = phaseSequence;
	}

	@Override
	public Lifecycle getLifecycle() {
		return this.lifecycle;
	}

	@Override
	public List<String> getPhaseSequence() {
		return this.phaseSequence;
	}

}
