package net.runeduniverse.tools.maven.r4m.pem.model;

import net.runeduniverse.lib.utils.logging.logs.Recordable;

public interface ExecutionRestriction<T> extends Recordable {

	public String getHint();

	public <D extends T> Class<D> getDataType();

	public boolean isActive(final T data);

}
