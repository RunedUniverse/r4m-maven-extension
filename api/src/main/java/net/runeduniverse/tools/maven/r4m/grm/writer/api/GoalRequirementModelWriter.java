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
package net.runeduniverse.tools.maven.r4m.grm.writer.api;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import net.runeduniverse.tools.maven.r4m.grm.model.GoalRequirementModel;

public interface GoalRequirementModelWriter {

	public void writeModel(final OutputStream stream, final GoalRequirementModel grm) throws IOException;

	public void writeModel(final OutputStream stream, final GoalRequirementModel grm, final Charset charset)
			throws IOException;

	public void writeModel(final OutputStream stream, final GoalRequirementModel grm, final String charsetName)
			throws IOException;

}
