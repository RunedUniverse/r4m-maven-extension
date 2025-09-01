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
package net.runeduniverse.tools.maven.r4m.mojo;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import net.runeduniverse.lib.utils.config.api.Property;
import net.runeduniverse.tools.maven.r4m.SettingsFactory;
import net.runeduniverse.tools.maven.r4m.api.Settings;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;
import static net.runeduniverse.lib.utils.common.ComparisonUtils.objectEquals;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.warnExtensionFeatureState;
import static net.runeduniverse.tools.maven.r4m.mojo.api.ExtensionUtils.supportsExtensionFeatures;

/**
 * prints the status-page
 *
 * @goal status
 * @requiresProject false
 * @requiresReports false
 * @threadSafe true
 * @since 1.0.0
 * @author VenaNocta
 */
public class StatusMojo extends AbstractMojo {

	private static final String ITEM_FORMAT = "\033[%c;%cm%s\033[m";
	private static final String PROPERTY_FORMAT = " %-53s = %s";

	private static final String T_DEFAULT = "key.default";
	private static final String T_SELECTED = "key";
	private static final Set<String> TEMPLATE = new LinkedHashSet<>();
	static {
		TEMPLATE.add(T_SELECTED);
		TEMPLATE.add(T_DEFAULT);
	}

	/**
	 * @component
	 */
	private Settings settings;
	/**
	 * @component
	 */
	private SettingsFactory settingsFactory;
	/**
	 * @parameter default-value="${session}"
	 * @readonly
	 */
	private MavenSession mvnSession;

	private boolean getFancyOutput() {
		final Property<Boolean> fancy = this.settings.getFancyOutput();
		if (fancy != null)
			return fancy.getSelected();
		this.settingsFactory.setup(this.mvnSession);
		return this.settings.getFancyOutput()
				.getSelected();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!supportsExtensionFeatures(this.settings))
			warnExtensionFeatureState(getLog());
		getLog().info("");
		final Boolean fancy = getFancyOutput();
		getLog().info("\033[1mRunes4Maven Status\033[m");
		if (fancy)
			getLog().info(" " + format(TEMPLATE, T_DEFAULT, T_SELECTED));
		final List<Property<?>> props = new LinkedList<>(this.settings.getAllProperties());
		props.sort(null);
		for (Property<?> prop : props)
			logTextEntry(prop, fancy);
		getLog().info("");
	}

	private void logTextEntry(Property<?> entry, Boolean fancy) {
		if (entry == null || isBlank(entry.getId()))
			return;
		getLog().info("");
		if (fancy) {
			getLog().info(" " + entry.getId());
			getLog().info("     = " + format(entry.getOptions(), entry.getDefault(), entry.getSelected()));
			return;
		}
		getLog().info(String.format(PROPERTY_FORMAT, entry.getId() + ".default", entry.getDefault()));
		getLog().info(String.format(PROPERTY_FORMAT, entry.getId(), entry.getSelected()));
		getLog().info("     " + format(entry.getOptions(), null, null));
	}

	private static String format(Collection<?> options, Object defaultValue, Object selectedValue) {
		final Set<Object> values = new LinkedHashSet<>(options);
		if (!isNull(defaultValue))
			values.add(defaultValue);
		if (!isNull(selectedValue))
			values.add(selectedValue);

		final StringBuilder result = new StringBuilder("[");
		for (Iterator<?> idx = values.iterator(); idx.hasNext();) {
			final Object value = idx.next();

			result.append(String.format(ITEM_FORMAT, objectEquals(value, defaultValue) ? '4' : '0',
					objectEquals(value, selectedValue) ? '1' : '2', value.toString()));

			if (idx.hasNext())
				result.append(", ");
		}
		return result.append(']')
				.toString();
	}

	private static boolean isNull(final Object o) {
		if (o instanceof String)
			return isBlank((String) o);
		return o == null;
	}

}
