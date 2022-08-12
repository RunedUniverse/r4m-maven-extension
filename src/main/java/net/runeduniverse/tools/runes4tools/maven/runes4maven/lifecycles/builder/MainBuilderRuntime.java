package net.runeduniverse.tools.runes4tools.maven.runes4maven.lifecycles.builder;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.runes4tools.maven.runes4maven.Runes4MavenProperties;

@Component(role = BuilderRuntime.class, hint = "builder-main")
public class MainBuilderRuntime implements BuilderRuntime {

	@Parameter(property = Runes4MavenProperties.LIFECYCLE.BUILDER.PARAM_SOURCE_DIR, defaultValue = "${project.build.sourceDirectory}", required = true)
	File sourceDirectory;
	@Parameter(property = Runes4MavenProperties.LIFECYCLE.BUILDER.PARAM_TARGET_DIR, defaultValue = "${project.build.outputDirectory}", required = true)
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
