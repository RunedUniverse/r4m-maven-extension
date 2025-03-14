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
package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.mapping.LifecyclePhase;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = Lifecycle.class, hint = HelpLifecycle.HINT)
public class HelpLifecycle extends Lifecycle {

	public static final String HINT = "help";
	public static final String[] PHASES = { "help" };

	public HelpLifecycle() {
		super(HINT, new ArrayList<>(Arrays.asList(PHASES)), new LinkedHashMap<String, LifecyclePhase>(1));
	}
}
