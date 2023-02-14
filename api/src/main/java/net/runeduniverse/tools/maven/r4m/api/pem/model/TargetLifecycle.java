package net.runeduniverse.tools.maven.r4m.api.pem.model;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.Recordable;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

public class TargetLifecycle implements Recordable {

	private String id;
	private String startPhase = null;
	private String stopPhase = null;

	public TargetLifecycle(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public String getStartPhase() {
		return this.startPhase;
	}

	public String getStopPhase() {
		return this.stopPhase;
	}

	public void setStartPhase(String value) {
		this.startPhase = value;
	}

	public void setStopPhase(String value) {
		this.stopPhase = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof TargetLifecycle))
			return false;
		TargetLifecycle lifecycle = (TargetLifecycle) obj;

		if (!this.id.equals(lifecycle.getId()))
			return false;

		if (this.startPhase == null) {
			if (lifecycle.getStartPhase() != null)
				return false;
		} else if (!this.startPhase.equals(lifecycle.getStartPhase()))
			return false;

		if (this.stopPhase == null) {
			if (lifecycle.getStopPhase() != null)
				return false;
		} else if (!this.stopPhase.equals(lifecycle.getStopPhase()))
			return false;

		return true;
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("Target Lifecycle");

		tree.append("id", this.id);

		if (!isBlank(this.startPhase))
			tree.append("start-phase", this.startPhase);

		if (!isBlank(this.stopPhase))
			tree.append("stop-phase", this.stopPhase);

		return tree;
	}
}
