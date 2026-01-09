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
package net.runeduniverse.tools.maven.r4m.pem.model;

import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;
import static net.runeduniverse.lib.utils.common.HashUtils.hash;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;

public class Execution implements DataEntry {

	public static final String HINT = "execution";
	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.pem.model.Execution";

	protected final Supplier<Set<ExecutionRestriction>> restrictionsSupplier;
	protected final Supplier<Set<ExecutionTrigger>> triggerSupplier;
	protected final Supplier<Map<String, Lifecycle>> lifecyclesSupplier;

	protected final Set<ExecutionRestriction> restrictions;
	protected final Set<ExecutionTrigger> trigger;
	protected final Map<String, Lifecycle> lifecycles;

	protected final String id;
	protected final ExecutionSource source;

	protected boolean inherited = true;
	protected boolean activeAlways = false;
	protected boolean activeDefault = false;
	protected boolean activeNever = false;

	public Execution(final String id, final ExecutionSource source) {
		this(LinkedHashSet::new, LinkedHashSet::new, LinkedHashMap::new, id, source);
	}

	public Execution(final Supplier<Set<ExecutionRestriction>> restrictionsSupplier,
			final Supplier<Set<ExecutionTrigger>> triggerSupplier,
			final Supplier<Map<String, Lifecycle>> lifecyclesSupplier, final String id, final ExecutionSource source) {
		this.restrictionsSupplier = restrictionsSupplier;
		this.triggerSupplier = triggerSupplier;
		this.lifecyclesSupplier = lifecyclesSupplier;
		this.restrictions = this.restrictionsSupplier.get();
		this.trigger = this.triggerSupplier.get();
		this.lifecycles = this.lifecyclesSupplier.get();
		this.id = id;
		this.source = source;
	}

	public String getId() {
		return this.id;
	}

	public ExecutionSource getSource() {
		return this.source;
	}

	public boolean isInherited() {
		return this.inherited;
	}

	public boolean isAlwaysActive() {
		return this.activeAlways;
	}

	public boolean isDefaultActive() {
		return this.activeDefault;
	}

	public boolean isNeverActive() {
		return this.activeNever;
	}

	public Set<ExecutionRestriction> getRestrictions() {
		return this.restrictions;
	}

	public Set<ExecutionTrigger> getTrigger() {
		return this.trigger;
	}

	public Lifecycle getLifecycle(final String lifecycleId) {
		return this.lifecycles.get(lifecycleId);
	}

	public Lifecycle computeLifecycleIfAbsent(final String lifecycleId,
			final Function<? super String, ? extends Lifecycle> mappingFunction) {
		return this.lifecycles.computeIfAbsent(lifecycleId, mappingFunction);
	}

	public Map<String, Lifecycle> getLifecycles() {
		return this.lifecycles;
	}

	public void setInherited(final boolean value) {
		this.inherited = value;
	}

	public void setAlwaysActive(final boolean value) {
		this.activeAlways = value;
	}

	public void setDefaultActive(final boolean value) {
		this.activeDefault = value;
	}

	public void setNeverActive(final boolean value) {
		this.activeNever = value;
	}

	public void addRestriction(final ExecutionRestriction value) {
		for (ExecutionRestriction restriction : this.restrictions) {
			if (restriction != null && restriction.equals(value))
				return;
		}
		this.restrictions.add(value);
	}

	public void addRestrictions(final Collection<ExecutionRestriction> values) {
		for (ExecutionRestriction value : values)
			addRestriction(value);
	}

	public void addTrigger(final ExecutionTrigger trigger) {
		this.trigger.add(trigger);
	}

	public void addTrigger(final Collection<ExecutionTrigger> values) {
		for (ExecutionTrigger value : values)
			addTrigger(value);
	}

	public void putLifecycle(final Lifecycle lifecycle) {
		this.lifecycles.put(lifecycle.getId(), lifecycle);
	}

	public void putLifecycles(final Collection<Lifecycle> lifecycles) {
		for (Lifecycle lifecycle : lifecycles)
			putLifecycle(lifecycle);
	}

	@Override
	public int hashCode() {
		return hash(type()) ^ hash(getId()) ^ hash(getSource());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Execution))
			return false;
		final Execution exec = (Execution) obj;

		return objectEquals(this.id, exec.getId()) //
				&& objectEquals(this.source, exec.getSource()) //
				&& objectEquals(this.inherited, exec.isInherited()) //
				&& objectEquals(this.activeAlways, exec.isAlwaysActive()) //
				&& objectEquals(this.activeDefault, exec.isDefaultActive()) //
				&& objectEquals(this.activeNever, exec.isNeverActive()) //
				&& objectEquals(this.restrictions, exec.getRestrictions()) //
				&& objectEquals(this.trigger, exec.getTrigger()) //
				&& objectEquals(this.lifecycles, exec.getLifecycles());
	}

	@Override
	public Execution copy() {
		final Execution exec = new Execution(this.restrictionsSupplier, this.triggerSupplier, this.lifecyclesSupplier,
				this.id, this.source);

		exec.setInherited(isInherited());
		exec.setAlwaysActive(isAlwaysActive());
		exec.setDefaultActive(isDefaultActive());
		exec.setNeverActive(isNeverActive());

		exec.addTrigger(getTrigger());
		exec.addRestrictions(getRestrictions());
		exec.putLifecycles(getLifecycles().values());

		return exec;
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("Execution");

		tree.append("id", this.id);
		tree.append("source", this.source.key());
		tree.append("inherited", Boolean.toString(this.inherited));

		final CompoundTree restrictionsTree = new DefaultCompoundTree("Restrictions");

		if (!this.restrictions.isEmpty()) {
			for (Recordable restriction : this.restrictions)
				restrictionsTree.append(restriction.toRecord());
			tree.append(restrictionsTree);
		}

		final CompoundTree triggerTree = new DefaultCompoundTree("Trigger");

		if (this.activeAlways)
			triggerTree.append("always-active", "true");
		if (this.activeDefault)
			triggerTree.append("default-active", "true");
		if (this.activeNever)
			triggerTree.append("never-active", "true");
		if (!this.trigger.isEmpty()) {
			for (Recordable trigger : this.trigger)
				triggerTree.append(trigger.toRecord());
		}

		if (triggerTree.hasContent())
			tree.append(triggerTree);

		for (Recordable lifecycle : this.lifecycles.values())
			tree.append(lifecycle.toRecord());

		return tree;
	}
}
