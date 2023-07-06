/*
 * Copyright © 2023 VenaNocta (venanocta@gmail.com)
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
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.lifecycle.api.PhaseSequenceCalculatorDelegate;

@Component(role = PhaseSequenceCalculatorDelegate.class, hint = DeclaredSeqCalculatorDelegate.HINT)
public class DeclaredSeqCalculatorDelegate implements PhaseSequenceCalculatorDelegate {

	public static final String HINT = "declared";

	@Override
	public List<String> calculatePhaseSequence(Lifecycle lifecycle, String phase) {
		List<String> sequence = new LinkedList<>();
		sequence.add(phase);
		return sequence;
	}

}
