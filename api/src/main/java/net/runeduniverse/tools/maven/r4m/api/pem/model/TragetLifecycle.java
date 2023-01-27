package net.runeduniverse.tools.maven.r4m.api.pem.model;

import java.util.List;

public class TragetLifecycle extends Target {
	List<String> executions;
	String startAtPhase;
	String stopAtPhase;
	List<String> excludePhases;
}
