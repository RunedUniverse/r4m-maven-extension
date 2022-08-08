package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.builder.mojos;

import org.apache.maven.plugin.AbstractMojo;
import org.codehaus.plexus.component.annotations.Requirement;

import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.builder.BuilderRuntime;
import net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.builder.ReferenceMap;

public abstract class AMainBuilderMojo extends AbstractMojo {

	@Requirement(role = BuilderRuntime.class, hint = "builder-main")
	protected BuilderRuntime runtime;
	
	@Requirement(role = ReferenceMap.class)
	protected ReferenceMap references;
}
