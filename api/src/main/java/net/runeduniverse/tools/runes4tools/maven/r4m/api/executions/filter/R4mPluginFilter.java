package net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.filter;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.executions.model.Plugin;

/**
 * Check if the MavenPlugin meets the filter
 * 
 * @author Pl4yingNight
 *
 */
@FunctionalInterface
public interface R4mPluginFilter {
	public boolean apply(Plugin r4mPlugin);

	default R4mPluginFilter and(R4mPluginFilter filter) {
		return new And(this, filter);
	}

	default R4mPluginFilter or(R4mPluginFilter filter) {
		return new Or(this, filter);
	}

	public static class And implements R4mPluginFilter {
		private final R4mPluginFilter a;
		private final R4mPluginFilter b;

		protected And(R4mPluginFilter a, R4mPluginFilter b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean apply(Plugin r4mPlugin) {
			return this.a.apply(r4mPlugin) && this.b.apply(r4mPlugin);
		}

	}

	public static class Or implements R4mPluginFilter {
		private final R4mPluginFilter a;
		private final R4mPluginFilter b;

		protected Or(R4mPluginFilter a, R4mPluginFilter b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean apply(Plugin r4mPlugin) {
			return this.a.apply(r4mPlugin) || this.b.apply(r4mPlugin);
		}

	}
}
