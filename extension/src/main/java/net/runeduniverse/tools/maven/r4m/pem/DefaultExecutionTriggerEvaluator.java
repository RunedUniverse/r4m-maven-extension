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
package net.runeduniverse.tools.maven.r4m.pem;

import static net.runeduniverse.lib.utils.common.ReflectionUtils.supplyWithHandler;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionTriggerEvaluationHandler;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionTriggerEvaluator;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;

@Component(role = ExecutionTriggerEvaluator.class, hint = "default")
public class DefaultExecutionTriggerEvaluator implements ExecutionTriggerEvaluator {

	public static int MAX_TYPE_SEARCH_DEPTH = 4;

	@Requirement(role = ExecutionTriggerEvaluationHandler.class)
	protected Map<String, ExecutionTriggerEvaluationHandler> handler;

	@Override
	public boolean isActive(ExecutionArchiveSelectorConfig config, ExecutionTrigger trigger) {
		if (config == null || trigger == null)
			return false;

		final Boolean result = supplyWithHandler(this.handler, trigger, h -> h.isActive(config, trigger),
				MAX_TYPE_SEARCH_DEPTH);

		// result can be null!
		return result == null ? false : result;
	}
}
