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
package net.runeduniverse.tools.maven.r4m.pem.writer.trigger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionTriggerWriter;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.trigger.ProfileTriggerProvided;

@Component(role = ExecutionTriggerWriter.class, hint = ProfileTriggerProvided.HINT)
public class ProfileTriggerProvidedWriter implements ExecutionTriggerWriter {

	@Override
	public boolean append(final PlexusConfiguration triggerNode, final ExecutionTrigger<?> trigger) {
		if (trigger == null || !(trigger instanceof ProfileTriggerProvided))
			return false;

		PlexusConfiguration node = triggerNode.getChild(ProfileTriggerProvided.HINT, true);
		node.setAttribute("id", ((ProfileTriggerProvided) trigger).getProfileId());

		return true;
	}

}
