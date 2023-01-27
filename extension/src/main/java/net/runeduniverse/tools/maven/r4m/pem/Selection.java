package net.runeduniverse.tools.maven.r4m.pem;

import java.util.Set;

import net.runeduniverse.lib.utils.logging.logs.CompoundTree;
import net.runeduniverse.tools.maven.r4m.api.pem.ExecutionArchiveSelection;
import net.runeduniverse.tools.maven.r4m.api.pem.view.ExecutionView;

public class Selection implements ExecutionArchiveSelection {

	private final Set<ExecutionView> views;

	public Selection(Set<ExecutionView> views) {
		this.views = views;
	}

	@Override
	public CompoundTree toRecord() {
		CompoundTree tree = new CompoundTree("Selection");

		for (ExecutionView view : views)
			tree.append(view.toRecord());

		return tree;
	}

	@Override
	public String toString() {
		return toRecord().toString();
	}

}
