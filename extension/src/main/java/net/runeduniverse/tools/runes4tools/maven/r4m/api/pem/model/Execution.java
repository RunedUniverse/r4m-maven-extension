package net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model;

import java.util.List;
import java.util.Map;

public class Execution {
	String id;
	ExecutionSource source;
	
	List<Trigger> trigger;
	boolean activeAlways;
	boolean activeNever;
	boolean activeOnCall;
	
	Map<String, Lifecycle> lifecycles;
}
