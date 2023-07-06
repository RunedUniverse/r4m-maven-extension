package net.runeduniverse.tools.maven.r4m.mojos;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import net.runeduniverse.tools.maven.r4m.api.Property;
import net.runeduniverse.tools.maven.r4m.api.Settings;

/**
 * prints the status-page
 *
 * @author VenaNocta
 * @goal status
 * @requiresProject false
 */
public class StatusMojo extends AbstractMojo {

	private static final String ITEM_FORMAT = "\033[%c;%cm%s\033[m";

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

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("");
		getLog().info("\033[1mRunes4Maven Status\033[m");
		getLog().info(" " + format(TEMPLATE, T_DEFAULT, T_SELECTED));
		for (Property<?> prop : this.settings.getAllProperties())
			logTextEntry(prop);
		getLog().info("");
		getLog().info("");
	}

	private void logTextEntry(Property<?> entry) {
		if (entry == null || isBlank(entry.getId()))
			return;
		getLog().info("");
		getLog().info(" " + entry.getId());
		getLog().info("     = " + format(entry.getOptions(), entry.getDefault(), entry.getSelected()));
	}

	private static String format(Collection<?> options, Object defaultValue, Object selectedValue) {
		Set<Object> values = new LinkedHashSet<>(options);
		if (!isNull(defaultValue))
			values.add(defaultValue);
		if (!isNull(selectedValue))
			values.add(selectedValue);

		StringBuilder result = new StringBuilder("[");
		for (Iterator<?> idx = values.iterator(); idx.hasNext();) {
			Object value = idx.next();

			result.append(String.format(ITEM_FORMAT, equals(value, defaultValue) ? '4' : '0',
					equals(value, selectedValue) ? '1' : '2', value.toString()));

			if (idx.hasNext())
				result.append(", ");
		}
		return result.append(']')
				.toString();
	}

	private static boolean isNull(Object o) {
		if (o instanceof String)
			return isBlank((String) o);
		return o == null;
	}

	private static boolean equals(Object a, Object b) {
		if (a == b)
			return true;
		if (a == null)
			return b.equals(a);
		return a.equals(b);
	}

}
