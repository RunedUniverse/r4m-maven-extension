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
package net.runeduniverse.tools.maven.r4m.pem.trigger;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionTriggerEvaluationHandler;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.model.ProfileTriggerMissing;

@Component(role = ExecutionTriggerEvaluationHandler.class, hint = ProfileTriggerMissing.CANONICAL_NAME)
public class ProfileMissingTriggerHandler implements ExecutionTriggerEvaluationHandler {

	@Override
	public boolean isActive(ExecutionArchiveSelectorConfig config, ExecutionTrigger entry) {
		if (!(entry instanceof ProfileTriggerMissing))
			return false;
		final ProfileTriggerMissing trigger = (ProfileTriggerMissing) entry;
		return !config.getProvidedProfiles()
				.contains(trigger.getProfileId());
	}
}
