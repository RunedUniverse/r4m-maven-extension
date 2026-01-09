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
package net.runeduniverse.tools.maven.r4m.pem.restrictions;

import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionRestrictionEvaluationHandler;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.PackagingProcedureRestriction;

import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;

import org.codehaus.plexus.component.annotations.Component;

@Component(role = ExecutionRestrictionEvaluationHandler.class, hint = PackagingProcedureRestriction.CANONICAL_NAME)
public class PackagingProcedureRestrictionHandler implements ExecutionRestrictionEvaluationHandler {

	@Override
	public boolean isActive(ExecutionArchiveSelectorConfig config, ExecutionRestriction entry) {
		if (!(entry instanceof PackagingProcedureRestriction))
			return false;
		final PackagingProcedureRestriction restriction = (PackagingProcedureRestriction) entry;
		return objectEquals(restriction.getPackagingProcedure(), config.getPackagingProcedure());
	}
}
