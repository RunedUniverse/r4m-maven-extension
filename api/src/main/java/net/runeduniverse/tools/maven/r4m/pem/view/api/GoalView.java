package net.runeduniverse.tools.maven.r4m.pem.view.api;

import java.util.Collection;
import java.util.Set;

import org.apache.maven.plugin.descriptor.MojoDescriptor;

import net.runeduniverse.lib.utils.logging.logs.Recordable;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;

public interface GoalView extends Recordable {

	public String getGroupId();

	public String getArtifactId();

	public String getGoalId();

	public Set<String> getModes();

	public boolean isOptional();

	public boolean hasFork();

	public boolean hasValidFork();

	public Fork getFork();

	public MojoDescriptor getDescriptor();

	public void addModes(String... modes);

	public void addModes(Collection<String> modes);

	public void setOptional(boolean optional);

	public void setFork(Fork fork);

	public void setDescriptor(MojoDescriptor descriptor);

}
