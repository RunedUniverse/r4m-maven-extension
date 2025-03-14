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
package net.runeduniverse.tools.maven.r4m.pem.restrictions;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;
import net.runeduniverse.tools.maven.r4m.pem.api.ExecutionArchiveSelectorConfig;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;

public class PackagingProcedureRestriction implements ExecutionRestriction<ExecutionArchiveSelectorConfig> {

	public static final String HINT = "packaging-procedure";

	protected final String procedure;

	public PackagingProcedureRestriction(String packagingProcedure) {
		this.procedure = packagingProcedure;
	}

	public String getPackagingProcedure() {
		return this.procedure;
	}

	@Override
	public String getHint() {
		return PackagingProcedureRestriction.HINT;
	}

	@Override
	public Class<ExecutionArchiveSelectorConfig> getDataType() {
		return ExecutionArchiveSelectorConfig.class;
	}

	@Override
	public boolean isActive(final ExecutionArchiveSelectorConfig config) {
		if (this.procedure == null)
			if (config.getPackagingProcedure() != null)
				return false;
		return this.procedure.equals(config.getPackagingProcedure());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PackagingProcedureRestriction))
			return false;
		final PackagingProcedureRestriction restriction = (PackagingProcedureRestriction) obj;
		if (this.procedure == null)
			return restriction.getPackagingProcedure() == null;
		return this.procedure.equals(restriction.getPackagingProcedure());
	}

	@Override
	public CompoundTree toRecord() {
		return new DefaultCompoundTree("packaging-procedure", this.procedure);
	}

}
