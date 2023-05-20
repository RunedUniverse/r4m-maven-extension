package net.runeduniverse.tools.maven.r4m.api;

import java.util.Set;

public interface Property<T> {

	public String getId();

	public T getDefault();

	public T getSelected();

	public Set<T> getOptions();

	public void setDefault(T value);

	public void setSelected(T value);

}
