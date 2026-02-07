/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.lifecycle.api;

import java.util.List;

import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecyclePhaseNotFoundException;

/**
 * Phase Sequence mapping delegate component interface. Calculates project build
 * {@link LifecycleTaskRequest} given the {@link LifecycleTaskData}.
 *
 * @since 0.0.0
 * @author VenaNocta
 */
public interface LifecycleTaskRequestCalculatorDelegate {

	public List<LifecycleTaskRequest> calculateTaskRequest(LifecycleTaskData taskData)
			throws LifecyclePhaseNotFoundException;

	public LifecycleTaskRequest calculateTaskRequest(Lifecycle lifecycle, String phaseId)
			throws LifecyclePhaseNotFoundException;

}
