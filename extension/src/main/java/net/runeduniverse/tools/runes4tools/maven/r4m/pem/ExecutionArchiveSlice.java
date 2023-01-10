package net.runeduniverse.tools.runes4tools.maven.r4m.pem;

import java.util.Map;

import org.apache.maven.project.MavenProject;

import net.runeduniverse.tools.runes4tools.maven.r4m.pem.model.Execution;

public class ExecutionArchiveSlice {
	MavenProject mvnProject;
	String version;
	Map<String, Execution> executions;
}
