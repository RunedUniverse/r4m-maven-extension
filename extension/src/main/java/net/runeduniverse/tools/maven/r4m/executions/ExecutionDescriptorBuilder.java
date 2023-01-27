package net.runeduniverse.tools.runes4tools.maven.r4m.executions;

import java.io.IOException;
import java.io.Reader;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import net.runeduniverse.tools.runes4tools.maven.r4m.Properties;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.ExecutionDescriptorBuilderInstance;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model.Execution;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model.ExecutionDescriptor;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model.Goal;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model.Phase;

public class ExecutionDescriptorBuilder {

	public ExecutionDescriptorBuilderInstance newInstance(ExecutionDescriptor descriptor) {
		return new Instance(new XmlPlexusConfiguration("executions"), Properties.METAINF.RUNES4MAVEN.EXECUTIONS);
	}

	public ExecutionDescriptorBuilderInstance newInstance(Reader reader, String source)
			throws XmlPullParserException, IOException {
		return new Instance(new XmlPlexusConfiguration(Xpp3DomBuilder.build(reader)), source);
	}

	protected static class Instance implements ExecutionDescriptorBuilderInstance {
		public PlexusConfiguration cnf;
		public String source;

		public Instance(PlexusConfiguration cnf, String source) {
			this.cnf = cnf;
			this.source = source;
		}

		public ExecutionDescriptor buildDescriptor() {
			ExecutionDescriptor descriptor = new ExecutionDescriptor();

			for (PlexusConfiguration execution : cnf.getChildren("execution"))
				descriptor.put(parseExecution(execution));

			return descriptor;
		}

		protected Execution parseExecution(PlexusConfiguration cnfExecution) {
			Execution execution = new Execution(cnfExecution.getChild("id")
					.getValue());
			for (PlexusConfiguration cnfPhase : cnfExecution.getChild("phases")
					.getChildren("phase"))
				execution.putPhase(parsePhase(cnfPhase));
			return execution;
		}

		protected Phase parsePhase(PlexusConfiguration cnfPhase) {
			Phase phase = new Phase(cnfPhase.getChild("id")
					.getValue());
			for (PlexusConfiguration cnfGoal : cnfPhase.getChild("goals")
					.getChildren("goal"))
				phase.addGoal(parseGoal(cnfGoal));
			return phase;
		}

		protected Goal parseGoal(PlexusConfiguration cnfGoal) {
			Goal goal = new Goal(cnfGoal.getValue());
			return goal;
		}
	}
}
