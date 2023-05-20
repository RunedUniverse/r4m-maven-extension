package net.runeduniverse.tools.maven.r4m.mojos;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import net.runeduniverse.tools.maven.r4m.api.Property;
import net.runeduniverse.tools.maven.r4m.api.Settings;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;;

/**
 * prints the status-page
 * 
 * @author VenaNocta
 * @goal status
 * @requiresProject false
 */
public class StatusMojo extends AbstractMojo {

	private static final String ITEM_FORMAT = "\033[%c;%cm%s\033[m";

	private static final String T_DEFAULT = "default";
	private static final String T_SELECTED = "selected";
	private static final Set<String> TEMPLATE = new LinkedHashSet<>();
	static {
		TEMPLATE.add(T_DEFAULT);
		TEMPLATE.add(T_SELECTED);
	}

	/**
	 * @component
	 */
	private Settings settings;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("");
		getLog().info("\033[1mRunes4Maven Status\033[m");
		getLog().info(" key = " + format(TEMPLATE, T_DEFAULT, T_SELECTED));

		Collections.addAll(new LinkedHashSet<>(), "declared", "sequential");

		// r4m.phase-sequence-calculator.default
		// r4m.phase-sequence-calculator.selected
		logTextEntry(this.settings.getPhaseSequenceCalculator());
		getLog().info("");
		getLog().info("");
	}

	private void logTextEntry(Property<String> entry) {
		if (entry == null || isBlank(entry.getId()) || entry.getOptions() == null || entry.getOptions()
				.isEmpty())
			return;
		getLog().info("");
		getLog().info(" " + entry.getId());
		getLog().info("     = " + format(entry.getOptions(), entry.getDefault(), entry.getSelected()));
	}

	private static String format(Collection<String> options, String base, String selected) {
		Set<String> values = new LinkedHashSet<>(options);
		if (!isBlank(base))
			values.add(base);
		if (!isBlank(selected))
			values.add(selected);

		StringBuilder result = new StringBuilder("[");
		for (Iterator<String> idx = values.iterator(); idx.hasNext();) {
			String value = idx.next();

			result.append(String.format(ITEM_FORMAT, value == base ? '4' : '0', value == selected ? '1' : '2', value));

			if (idx.hasNext())
				result.append(", ");
		}
		return result.append(']')
				.toString();
	}

}
