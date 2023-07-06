package net.runeduniverse.tools.maven.r4m.pem.model;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

public class ExecutionSource {

	@Deprecated
	public static final Map<String, ExecutionSource> KNOWN_SOURCES = new LinkedHashMap<>(4);

	public static final ExecutionSource OVERRIDE = new ExecutionSource("override");
	public static final ExecutionSource PACKAGING = new ExecutionSource("packaging");
	public static final ExecutionSource PLUGIN = new ExecutionSource("plugin");
	@Deprecated
	public static final ExecutionSource EFFECTIVE = new ExecutionSource("effective");

	private final String key;

	protected ExecutionSource(String key) {
		this.key = key;
		ExecutionSource.KNOWN_SOURCES.put(key, this);
	}

	public String key() {
		return this.key;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ExecutionSource))
			return false;

		return this.key.equals(((ExecutionSource) obj).key());
	}

	@Override
	public String toString() {
		return this.key;
	}

	public static ExecutionSource create(String key) {
		if (isBlank(key))
			return null;

		ExecutionSource source = ExecutionSource.KNOWN_SOURCES.get(key);
		if (source == null)
			return new ExecutionSource(key);
		else
			return source;
	}

}
