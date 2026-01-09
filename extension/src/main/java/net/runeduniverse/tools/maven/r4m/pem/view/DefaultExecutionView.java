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
package net.runeduniverse.tools.maven.r4m.pem.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.Recordable;
import net.runeduniverse.tools.maven.r4m.pem.view.api.ExecutionView;
import net.runeduniverse.tools.maven.r4m.pem.view.api.LifecycleView;

public class DefaultExecutionView implements ExecutionView {

	private final Map<String, LifecycleView> lifecycles = new LinkedHashMap<>();
	private final String id;

	public DefaultExecutionView(final String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Map<String, LifecycleView> getLifecycles() {
		return this.lifecycles;
	}

	@Override
	public void put(final LifecycleView view) {
		this.lifecycles.put(view.getId(), view);
	}

	@Override
	public LifecycleView getLifecycle(final String lifecycleId) {
		return this.lifecycles.get(lifecycleId);
	}

	@Override
	public LifecycleView computeLifecycleIfAbsent(final String lifecycleId,
			final Function<String, ? extends LifecycleView> mappingFunction) {
		return this.lifecycles.computeIfAbsent(lifecycleId, mappingFunction);
	}

	@Override
	public CompoundTree toRecord() {
		final CompoundTree tree = new DefaultCompoundTree("ExecutionView");

		tree.append("id", this.id);

		for (Recordable lifecycle : this.lifecycles.values())
			tree.append(lifecycle.toRecord());

		return tree;
	}

}
