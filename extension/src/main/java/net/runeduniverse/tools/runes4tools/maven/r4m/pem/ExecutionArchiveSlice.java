package net.runeduniverse.tools.runes4tools.maven.r4m.pem;

import java.util.List;
import java.util.Map;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Execution;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.ExecutionSource;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.view.ExecutionView;

public class ExecutionArchiveSlice {
	MavenProject mvnProject;
	String version;
	Map<String, Map<ExecutionSource, Execution>> executions;

	public List<ExecutionView> getEffectiveExecutions(String packaging, List<String> availableProfiles,
			List<String> activeProfiles) {

		return null;
	}
}
