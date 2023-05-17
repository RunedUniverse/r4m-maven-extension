package net.runeduniverse.tools.maven.r4m.eventspy.api;

import java.util.Collection;

import org.apache.maven.model.Plugin;

public interface MavenPluginPatchingEvent extends PatchingEvent {

	public Collection<Plugin> getEffectedPlugins();

	public static MavenPluginEvent createInfoEvent(Type type, Collection<Plugin> effectedPlugins) {
		return new MavenPluginEvent(type, null, effectedPlugins);
	}

	public static MavenPluginEvent createErrorEvent(Type type, Exception exception,
			Collection<Plugin> effectedPlugins) {
		return new MavenPluginEvent(type, exception, effectedPlugins);
	}

	public class MavenPluginEvent extends PatchingEvent.BasicEvent implements MavenPluginPatchingEvent {

		protected Collection<Plugin> effectedPlugins = null;

		public MavenPluginEvent(Type type, Exception exception, Collection<Plugin> effectedPlugins) {
			super(type, exception);
			this.effectedPlugins = effectedPlugins;
		}

		@Override
		public Collection<Plugin> getEffectedPlugins() {
			return effectedPlugins;
		}

	}

}
