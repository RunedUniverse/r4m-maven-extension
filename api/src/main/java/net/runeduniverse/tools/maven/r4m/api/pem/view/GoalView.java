package net.runeduniverse.tools.maven.r4m.api.pem.view;

import java.util.Collection;
import java.util.Set;

import org.apache.maven.plugin.descriptor.MojoDescriptor;

import net.runeduniverse.tools.maven.r4m.api.pem.Recordable;
import net.runeduniverse.tools.maven.r4m.api.pem.model.Fork;

public interface GoalView extends Recordable {
	public String getGroupId();

	public String getArtifactId();

	public String getGoalId();

	public Set<String> getModes();

	public boolean hasFork();

	public Fork getFork();

	public MojoDescriptor getDescriptor();

	public void addModes(String... modes);

	public void addModes(Collection<String> modes);

	public void setFork(Fork fork);

	public void setDescriptor(MojoDescriptor descriptor);
}
