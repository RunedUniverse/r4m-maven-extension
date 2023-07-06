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
package net.runeduniverse.tools.maven.r4m.pem.view.api;

import java.util.Collection;
import java.util.Set;

import org.apache.maven.plugin.descriptor.MojoDescriptor;

import net.runeduniverse.lib.utils.logging.logs.Recordable;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;

public interface GoalView extends Recordable {

	public String getGroupId();

	public String getArtifactId();

	public String getGoalId();

	public Set<String> getModes();

	public boolean isOptional();

	public boolean hasFork();

	public boolean hasValidFork();

	public Fork getFork();

	public MojoDescriptor getDescriptor();

	public void addModes(String... modes);

	public void addModes(Collection<String> modes);

	public void setOptional(boolean optional);

	public void setFork(Fork fork);

	public void setDescriptor(MojoDescriptor descriptor);

}
