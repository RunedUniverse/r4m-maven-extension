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

@Component(role = LifecycleTaskRequestCalculatorDelegate.class, hint = DeclaredLifecycleTaskReqCalcDelegate.HINT)
public class DeclaredLifecycleTaskReqCalcDelegate extends DefaultLifecycleTaskReqCalcDelegate
		implements LifecycleTaskRequestCalculatorDelegate {

	public static final String HINT = "declared";

	@Override
	public LifecycleTaskRequest calculateTaskRequest(final Lifecycle lifecycle, final String phaseId)
			throws LifecyclePhaseNotFoundException {
		List<String> sequence = new LinkedList<>();
		sequence.add(phaseId);
		return new DefaultLifecycleTaskRequest(lifecycle, sequence);
	}

	@Override
	protected void modifyEntry(final Entry entry, final boolean plain) {
		// Nothing to modify!
	}
}
