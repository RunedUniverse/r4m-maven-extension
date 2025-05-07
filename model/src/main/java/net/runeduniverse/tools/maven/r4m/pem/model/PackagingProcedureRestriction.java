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
package net.runeduniverse.tools.maven.r4m.pem.model;

import net.runeduniverse.lib.utils.logging.log.DefaultCompoundTree;
import net.runeduniverse.lib.utils.logging.log.api.CompoundTree;

import static net.runeduniverse.lib.utils.common.HashUtils.hash;
import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;

public class PackagingProcedureRestriction implements ExecutionRestriction {

	public static final String HINT = "packaging-procedure";
	public static final String CANONICAL_NAME = "net.runeduniverse.tools.maven.r4m.pem.model.PackagingProcedureRestriction";

	protected final String procedure;

	public PackagingProcedureRestriction(final String packagingProcedure) {
		this.procedure = packagingProcedure;
	}

	public String getPackagingProcedure() {
		return this.procedure;
	}

	@Override
	public int hashCode() {
		return hash(HINT) ^ hash(getPackagingProcedure());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PackagingProcedureRestriction))
			return false;
		final PackagingProcedureRestriction restriction = (PackagingProcedureRestriction) obj;
		return objectEquals(this.procedure, restriction.getPackagingProcedure());
	}

	@Override
	public PackagingProcedureRestriction copy() {
		final PackagingProcedureRestriction restriction = new PackagingProcedureRestriction(getPackagingProcedure());
		return restriction;
	}

	@Override
	public CompoundTree toRecord() {
		return new DefaultCompoundTree("packaging-procedure", this.procedure);
	}
}
