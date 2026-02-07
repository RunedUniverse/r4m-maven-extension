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
package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.maven.lifecycle.DefaultLifecycles;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecyclePhaseNotFoundException;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.api.Runes4MavenProperties;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskData;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskRequest;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.LifecycleTaskRequestCalculatorDelegate;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

public abstract class DefaultLifecycleTaskReqCalcDelegate implements LifecycleTaskRequestCalculatorDelegate {

	public static final String ERR_UNKNOWN_LIFECYCLE_PHASE = //
			"[%s] Unknown lifecycle phase \"%s\". "
					+ "You must specify a valid lifecycle task or phase in the format [<modes>/]<lifecycle-tasks>[@<executions>] "
					+ "or a goal in the format <plugin-prefix>:<goal>[@<execution>] or"
					+ " <plugin-group-id>:<plugin-artifact-id>[:<plugin-version>]:<goal>[@<execution>]. "
					+ "Additional help to lifecycle tasks can be acquired via r4m:help-task / "
					+ "Available lifecycle phases are: %s.";

	@Requirement
	private DefaultLifecycles defaultLifeCycles;

	// modify Entry after parsing
	protected abstract void modifyEntry(final Entry entry, final boolean plain);

	protected Entry parseEntry(String phase) {
		final Entry entry = new Entry(false, false, true);
		boolean flagged = false;

		// marker before the phase
		if (phase.indexOf('[') == 0) {
			entry.setAfter(true);
			flagged = true;
			phase = phase.substring(1);
		} else if (phase.indexOf(']') == 0) {
			entry.setBefore(true);
			entry.setInclude(false);
			flagged = true;
			phase = phase.substring(1);
		}

		// marker after the phase
		final int markerIdx = phase.length() - 1;
		if (phase.indexOf(']') == markerIdx) {
			if (!flagged)
				entry.setBefore(true);
			entry.setInclude(true);
			entry.setAfter(false);
			flagged = true;
			phase = phase.substring(0, markerIdx);
		} else if (phase.indexOf('[') == markerIdx) {
			if (!flagged)
				entry.setInclude(false);
			entry.setAfter(true);
			flagged = true;
			phase = phase.substring(0, markerIdx);
		}
		entry.setPhase(phase);
		modifyEntry(entry, flagged == false);
		return entry;
	}

	protected Lifecycle selectLifecycle(final String phase) throws LifecyclePhaseNotFoundException {
		final Lifecycle lifecycle = this.defaultLifeCycles.get(phase);
		if (lifecycle == null) {
			throw new LifecyclePhaseNotFoundException(String.format(ERR_UNKNOWN_LIFECYCLE_PHASE,
					Runes4MavenProperties.PREFIX_ID, phase, this.defaultLifeCycles.getLifecyclePhaseList()), phase);
		}
		return lifecycle;
	}

	protected List<Entry> splitEntries(String task) throws LifecyclePhaseNotFoundException {
		final List<Entry> entries = new LinkedList<>();
		String phase = null;

		while (task != null) {
			int splitIdx = task.indexOf(',');
			if (0 < splitIdx) {
				phase = task.substring(0, splitIdx);
				task = task.substring(splitIdx + 1);
			} else {
				phase = task;
				task = null;
			}
			final Entry entry = parseEntry(phase);
			if (entry == null)
				continue;
			phase = entry.getPhase();
			if (isBlank(phase))
				continue;
			entry.setLifecycle(selectLifecycle(phase));
			entries.add(entry);
		}
		return entries;
	}

	protected boolean checkOrder(final Lifecycle lifecycle, final String firstPhase, final String secondPhase) {
		boolean found = false;
		for (String phase : lifecycle.getPhases()) {
			if (phase == null)
				continue;
			if (phase.equals(firstPhase))
				found = true;
			if (phase.equals(secondPhase)) {
				if (found)
					return true;
				else
					return false;
			}
		}
		return false;
	}

	protected LifecycleTaskRequest calculateTaskRequest(final Lifecycle lifecycle, final String startPhaseId,
			final String endPhaseId, final boolean before, final boolean includeStartPhase, final boolean between,
			final boolean includeEndPhase, final boolean after) {
		final List<String> sequence = new LinkedList<>();
		short stage = 0;
		for (String phase : lifecycle.getPhases()) {
			if (phase.equals(startPhaseId)) {
				if (phase.equals(endPhaseId))
					stage = 3;
				else
					stage = 1;
			} else if (phase.equals(endPhaseId)) {
				stage = 3;
			}
			switch (stage) {
			case 0:
				if (before)
					sequence.add(phase);
				break;
			case 1:
				if (includeStartPhase)
					sequence.add(phase);
				stage = 2;
				break;
			case 2:
				if (between)
					sequence.add(phase);
				break;
			case 3:
				if (includeEndPhase)
					sequence.add(phase);
				stage = 4;
				break;
			case 4:
				if (after)
					sequence.add(phase);
			default:
				break;
			}
		}

		return new DefaultLifecycleTaskRequest(lifecycle, sequence);
	}

	@Override
	public List<LifecycleTaskRequest> calculateTaskRequest(final LifecycleTaskData taskData)
			throws LifecyclePhaseNotFoundException {
		final List<LifecycleTaskRequest> requests = new LinkedList<>();
		final List<Entry> entries = splitEntries(taskData.getLifecycleTask());

		// remove all redundant entries
		for (ListIterator<Entry> i = entries.listIterator(); i.hasNext();) {
			final Entry entry = i.next();
			if (entry == null) {
				i.remove();
				continue;
			}
			if (!entry.getInclude() || i.previousIndex() == -1 || i.nextIndex() == entries.size())
				continue;
			final Entry previous = entries.get(i.previousIndex());
			final Entry next = entries.get(i.nextIndex());
			final Lifecycle lifecycle = entry.getLifecycle();
			if (!lifecycle.equals(previous.getLifecycle()) || !lifecycle.equals(next.getLifecycle())
					|| !checkOrder(lifecycle, previous.getPhase(), entry.getPhase())
					|| !checkOrder(lifecycle, entry.getPhase(), next.getPhase()))
				continue;
			if (previous.getAfter() && entry.getBefore() && entry.getAfter() && next.getBefore())
				i.remove();
		}

		// calculate TaskRequest's
		boolean inSequence = false;
		for (ListIterator<Entry> i = entries.listIterator(); i.hasNext();) {
			final Entry startEntry = i.next();
			final Entry endEntry;
			final boolean hasNext;
			if (i.hasNext()) {
				endEntry = i.next();
				hasNext = i.hasNext();
				i.previous();
			} else {
				endEntry = null;
				hasNext = false;
			}
			final Lifecycle lifecycle = startEntry.getLifecycle();

			// check for end of sequence
			if (endEntry == null || !equalsLifecycle(lifecycle, endEntry.getLifecycle())) {
				requests.add(calculateTaskRequest(lifecycle, startEntry.getPhase(), startEntry.getPhase(),
						startEntry.getBefore(), false, false, startEntry.getInclude(), startEntry.getAfter()));
				inSequence = false;
				break;
			}

			// handle sequence start if not in sequence
			requests.add(calculateTaskRequest(lifecycle, startEntry.getPhase(), endEntry.getPhase(),
					inSequence ? false : startEntry.getBefore(), startEntry.getInclude(),
					startEntry.getAfter() || endEntry.getBefore(), endEntry.getInclude(),
					hasNext ? false : endEntry.getAfter()));

			if (!hasNext)
				return requests;
			inSequence = true;
		}

		return requests;
	}

	protected boolean equalsLifecycle(final Lifecycle a, final Lifecycle b) {
		if (a == b)
			return true;
		if (a == null || b == null)
			return false;
		return a.equals(b);
	}

	protected class Entry {

		protected String phase;
		protected Lifecycle lifecycle;
		protected boolean before;
		protected boolean after;
		protected boolean include;

		@Override
		public String toString() {
			return String.format("%s>%s (b: %b, i: %b, a: %b)", lifecycle == null ? "?" : lifecycle.getId(), phase,
					before, include, after);
		}

		public Entry() {
			this(null, null);
		}

		public Entry(final String phase, final Lifecycle lifecycle) {
			this(phase, lifecycle, false, false, true);
		}

		public Entry(final boolean before, final boolean after, final boolean include) {
			this(null, null, before, after, include);
		}

		public Entry(final String phase, final Lifecycle lifecycle, final boolean before, final boolean after,
				final boolean include) {
			this.phase = phase;
			this.lifecycle = lifecycle;
			this.before = before;
			this.after = after;
			this.include = include;
		}

		public String getPhase() {
			return this.phase;
		}

		public Lifecycle getLifecycle() {
			return this.lifecycle;
		}

		public boolean getBefore() {
			return this.before;
		}

		public boolean getAfter() {
			return this.after;
		}

		public boolean getInclude() {
			return this.include;
		}

		public void setPhase(final String phase) {
			this.phase = phase;
		}

		public void setLifecycle(final Lifecycle lifecycle) {
			this.lifecycle = lifecycle;
		}

		public void setBefore(final boolean before) {
			this.before = before;
		}

		public void setAfter(final boolean after) {
			this.after = after;
		}

		public void setInclude(final boolean include) {
			this.include = include;
		}
	}
}
