package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.util.LinkedList;
import java.util.List;
import org.apache.maven.lifecycle.Lifecycle;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.api.lifecycle.PhaseSequenceCalculatorDelegate;

@Component(role = PhaseSequenceCalculatorDelegate.class, hint = SequentialSeqCalculatorDelegate.HINT)
public class SequentialSeqCalculatorDelegate implements PhaseSequenceCalculatorDelegate {

	public static final String HINT = "sequential";

	@Override
	public List<String> calculatePhaseSequence(Lifecycle lifecycle, String lifecyclePhase) {
		List<String> sequence = new LinkedList<>();

		for (String phase : lifecycle.getPhases()) {
			sequence.add(phase);
			if (phase.equals(lifecyclePhase))
				break;
		}
		return sequence;
	}

}
