package net.runeduniverse.tools.maven.r4m.eventspy;

import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.lifecycle.api.MojoExecutionData;
import net.runeduniverse.tools.maven.r4m.pem.model.Fork;

@Component(role = EventSpy.class, hint = MojoExecutionEventSpy.HINT)
public class MojoExecutionEventSpy implements EventSpy {

	public static final String HINT = Properties.PREFIX_ID + "-eventspy-exec-mojo";

	@Requirement
	private Logger log;

	@Override
	public void init(Context context) throws Exception {
	}

	@Override
	public void onEvent(Object eventObj) throws Exception {

		if (!(eventObj instanceof ExecutionEvent))
			return;

		ExecutionEvent event = (ExecutionEvent) eventObj;

		switch (event.getType()) {
		case ForkStarted:
			handleForkStarted(event);
			return;
		default:
			return;
		}
	}

	@Override
	public void close() throws Exception {
	}

	private void handleForkStarted(ExecutionEvent event) {
		if (!(event.getMojoExecution() instanceof MojoExecutionData))
			return;
		final MojoExecution mojoExec = event.getMojoExecution();
		MojoExecutionData data = (MojoExecutionData) event.getMojoExecution();

		if (data.getLifecycleOverlayOrigin() != null)
			infoConfigurationOverrideApplied(mojoExec, data.getFork(), data.getLifecycleOverlayOrigin());
	}

	private void infoConfigurationOverrideApplied(final MojoExecution mojoExec, final Fork fork,
			final PluginDescriptor overlay) {
		this.log.info("");
		this.log.info(String.format("\033[1m Injected Configuration-Override into forked lifecycle\033[m",
				Properties.PREFIX_ID));
		this.log.info(String.format("\033[1m   provided by\033[0m %s:%s:%s", overlay.getGroupId(),
				overlay.getArtifactId(), overlay.getVersion()));
	}

}
