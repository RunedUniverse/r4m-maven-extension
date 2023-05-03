package net.runeduniverse.tools.maven.r4m.api.lifecycle;

import java.util.List;

import org.apache.maven.lifecycle.Lifecycle;

/**
 * Phase Sequence mapping delegate component interface. Calculates project build
 * phase sequence plan given {@link Lifecycle} and lifecycle phase.
 *
 * @since 0.0.0
 * @author VenaNocta
 */
public interface PhaseSequenceCalculatorDelegate {

	public List<String> calculatePhaseSequence(final Lifecycle lifecycle, final String phase);

}
