package net.runeduniverse.tools.maven.r4m.pem;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.maven.r4m.api.pem.PluginExecutionRegistry;
import net.runeduniverse.tools.maven.r4m.api.pem.PluginExecutionRegistrySlice;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = PluginExecutionRegistry.class, instantiationStrategy = "keep-alive")
public class Registry implements PluginExecutionRegistry {

	private Map<String, PluginExecutionRegistrySlice> prefixedSlices = new LinkedHashMap<>();
	private Map<String, PluginExecutionRegistrySlice> keyedSlices = new LinkedHashMap<>();
	private Map<String, PluginDescriptor> prefixedDescriptor = new LinkedHashMap<>();
	private Map<String, PluginDescriptor> keyedDescriptor = new LinkedHashMap<>();

	@Override
	public PluginExecutionRegistrySlice createSlice(PluginDescriptor mvnPluginDescriptor) {
		String prefix = mvnPluginDescriptor.getGoalPrefix();
		String key = createKey(mvnPluginDescriptor.getGroupId(), mvnPluginDescriptor.getArtifactId());

		PluginExecutionRegistrySlice slice = new RegistrySlice();
		if (!isBlank(prefix)) {
			this.prefixedDescriptor.put(prefix, mvnPluginDescriptor);
			this.prefixedSlices.put(prefix, slice);
		}

		this.keyedDescriptor.put(key, mvnPluginDescriptor);
		this.keyedSlices.put(key, slice);

		return slice;
	}

	@Override
	public boolean hasSlice(String prefix) {
		return this.prefixedSlices.containsKey(prefix);
	}

	@Override
	public boolean hasSlice(String groupId, String artifactId) {
		return this.prefixedSlices.containsKey(createKey(groupId, artifactId));
	}

	@Override
	public PluginExecutionRegistrySlice getSlice(String prefix) {
		return this.prefixedSlices.get(prefix);
	}

	@Override
	public PluginExecutionRegistrySlice getSlice(String groupId, String artifactId) {
		return this.keyedSlices.get(createKey(groupId, artifactId));
	}

	protected static String createKey(String groupId, String artifactId) {
		return String.join(":", groupId, artifactId);
	}
}
