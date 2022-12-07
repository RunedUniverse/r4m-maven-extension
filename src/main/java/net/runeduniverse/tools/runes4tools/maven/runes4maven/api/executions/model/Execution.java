package net.runeduniverse.tools.runes4tools.maven.runes4maven.api.executions.model;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

import java.util.LinkedHashMap;
import java.util.Map;

//import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.api.MavenProperties;

public class Execution {

	//private final CompoundTree record = new CompoundTree("Execution");
	private final String id;

	private final Map<String, Phase> phases;

	public Execution(final String id) {
		this.id = isBlank(id) ? MavenProperties.DEFAULT_EXECUTION_ID : id;
		this.phases = new LinkedHashMap<>();
		//this.record.append("id", this.id);
	}

	public String getId() {
		return this.id;
	}

	public Phase getPhase(String id) {
		return this.phases.get(id);
	}

	public Map<String, Phase> getPhases() {
		return this.phases;
	}

	public void putPhase(Phase phase) {
		this.phases.put(phase.getId(), phase);
		//phase.toRecord(this.record);
	}

	//public void toRecord(CompoundTree record) {
	//	record.append(this.record);
	//}
}
