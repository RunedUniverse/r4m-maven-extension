package net.runeduniverse.tools.maven.r4m.lifecycle;

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.lifecycle.Lifecycle;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.api.lifecycle.PhaseSequenceCalculatorDelegate;

@Component(role = PhaseSequenceCalculatorDelegate.class, hint = DeclaredSeqCalculatorDelegate.HINT)
public class DeclaredSeqCalculatorDelegate implements PhaseSequenceCalculatorDelegate {

	public static final String HINT = "declared";

	@Override
	public List<String> calculatePhaseSequence(Lifecycle lifecycle, String phase) {
		List<String> sequence = new LinkedList<>();
		sequence.add(phase);
		return sequence;
	}

}
