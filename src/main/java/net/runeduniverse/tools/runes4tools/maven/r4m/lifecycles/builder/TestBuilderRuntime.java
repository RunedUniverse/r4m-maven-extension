package net.runeduniverse.tools.runes4tools.maven.r4m.lifecycles.builder;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.runes4tools.maven.r4m.Properties;

@Component(role = BuilderRuntime.class, hint = "builder-test")
public class TestBuilderRuntime implements BuilderRuntime {

	@Parameter(property = Properties.LIFECYCLE.BUILDER.PARAM_TEST_SOURCE_DIR, defaultValue = Properties.LIFECYCLE.BUILDER.DEFAULT_VAL_TEST_SOURCE_DIR, required = true)
	File sourceDirectory;
	@Parameter(property = Properties.LIFECYCLE.BUILDER.PARAM_TARGET_DIR, defaultValue = Properties.LIFECYCLE.BUILDER.DEFAULT_VAL_TARGET_DIR, required = true)
	File targetDirectory;

	@Override
	public File getSourceDirectory() {
		return this.sourceDirectory;
	}

	@Override
	public File getTargetDirectory() {
		return this.targetDirectory;
	}

}
