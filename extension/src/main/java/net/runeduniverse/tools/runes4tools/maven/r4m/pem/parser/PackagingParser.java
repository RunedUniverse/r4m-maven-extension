package net.runeduniverse.tools.runes4tools.maven.r4m.pem.parser;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;

import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.ProjectExecutionModelPackagingParser;
import net.runeduniverse.tools.runes4tools.maven.r4m.api.pem.model.Execution;

@Component(role = ProjectExecutionModelPackagingParser.class, hint = "default")
public class PackagingParser implements ProjectExecutionModelPackagingParser {

	@Override
	public Set<Execution> parse() {
		// TODO Auto-generated method stub
		return new HashSet<>(0);
	}

}
