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
package net.runeduniverse.tools.maven.r4m.api;

import java.util.Collection;

public interface Settings {

	public Collection<Property<?>> getAllProperties();

	public void selectDefaults();

	public Property<String> getPhaseSequenceCalculator();

	public Property<String> getMissingBuildPluginHandler();

	public Property<Boolean> getPatchMojoOnFork();

	public Property<Boolean> getGeneratePluginExecutions();

	public Property<Boolean> getGeneratePluginExecutionsOnFork();

	public void setPhaseSequenceCalculator(Property<String> value);

	public void setMissingBuildPluginHandler(Property<String> value);

	public void setPatchMojoOnFork(Property<Boolean> value);

	public void setGeneratePluginExecutions(Property<Boolean> value);

	public void setGeneratePluginExecutionsOnFork(Property<Boolean> value);

}
