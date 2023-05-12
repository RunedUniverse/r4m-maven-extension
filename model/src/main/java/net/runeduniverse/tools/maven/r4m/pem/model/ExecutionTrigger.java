package net.runeduniverse.tools.maven.r4m.pem.model;

import net.runeduniverse.lib.utils.logging.logs.Recordable;

public interface ExecutionTrigger<T> extends Recordable {

	public String getHint();

	public Class<T> getDataType();

	public boolean isActive(final T data);

}
