package net.runeduniverse.tools.runes4tools.maven.r4m.pem.model;

import java.util.List;

public class TragetLifecycle extends Target {
	List<String> executions;
	String startAtPhase;
	String stopAtPhase;
	List<String> excludePhases;
}
