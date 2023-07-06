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
package net.runeduniverse.tools.maven.r4m.pem.parser.restrictions;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionRestrictionParser;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.restrictions.PackagingProcedureRestriction;

@Component(role = ExecutionRestrictionParser.class, hint = PackagingProcedureRestriction.HINT)
public class PackagingProcedureRestrictionParser implements ExecutionRestrictionParser {

	@Override
	public ExecutionRestriction<ExecutionArchiveSelectorConfig> parse(final PlexusConfiguration node) {
		String procedure = node.getValue();
		if (isBlank(procedure))
			return null;
		return new PackagingProcedureRestriction(procedure);
	}

}
