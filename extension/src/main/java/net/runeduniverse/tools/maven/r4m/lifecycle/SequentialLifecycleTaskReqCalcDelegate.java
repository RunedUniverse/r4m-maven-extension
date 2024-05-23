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

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecyclePhaseNotFoundException;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskRequest;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskRequestCalculatorDelegate;

@Component(role = LifecycleTaskRequestCalculatorDelegate.class, hint = SequentialLifecycleTaskReqCalcDelegate.HINT)
public class SequentialLifecycleTaskReqCalcDelegate extends DefaultLifecycleTaskReqCalcDelegate
		implements LifecycleTaskRequestCalculatorDelegate {

	public static final String HINT = "sequential";

	@Override
	protected List<Entry> splitEntries(String task) throws LifecyclePhaseNotFoundException {
		List<Entry> entries = new LinkedList<>();
		int markerIdx = -1;
		Entry entry = null;
		String phase = null;
		boolean flagged = false;

		while (task != null) {
			flagged = false;
			entry = new Entry(false, false, true);
			if (task.indexOf('[') == 0) {
				flagged = true;
				entry.setAfter(true);
				task = task.substring(1);
			}
			if (task.indexOf(']') == 0) {
				flagged = true;
				entry.setBefore(true);
				entry.setInclude(false);
				task = task.substring(1);
			}

			int splitIdx = task.indexOf(',');
			if (0 < splitIdx) {
				task = task.substring(splitIdx + 1);
				phase = task.substring(0, splitIdx);
			} else {
				phase = task;
				task = null;
			}

			markerIdx = phase.indexOf('[');
			if (markerIdx == phase.length() - 1) {
				flagged = true;
				entry.setAfter(true);
				entry.setInclude(false);
				phase = phase.substring(0, markerIdx);
			}
			markerIdx = phase.indexOf(']');
			if (markerIdx == phase.length() - 1 || !flagged) {
				entry.setBefore(true);
				phase = phase.substring(0, markerIdx);
			}
			entry.setLifecycle(selectLifecycle(phase));
			entry.setPhase(phase);
			entries.add(entry);
		}
		return entries;
	}

	@Override
	public LifecycleTaskRequest calculateTaskRequest(final Lifecycle lifecycle, final String phaseId)
			throws LifecyclePhaseNotFoundException {
		List<String> sequence = new LinkedList<>();
		for (String phase : lifecycle.getPhases()) {
			sequence.add(phase);
			if (phase.equals(phaseId))
				break;
		}
		return new DefaultLifecycleTaskRequest(lifecycle, sequence);
	}

}
