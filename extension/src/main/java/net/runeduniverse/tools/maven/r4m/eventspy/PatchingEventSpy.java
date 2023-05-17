package net.runeduniverse.tools.maven.r4m.eventspy;

import org.apache.maven.eventspy.EventSpy;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import net.runeduniverse.tools.maven.r4m.Properties;
import net.runeduniverse.tools.maven.r4m.eventspy.api.PatchingEvent;
import net.runeduniverse.tools.maven.r4m.eventspy.api.PatchingEvent.Type;

@Component(role = EventSpy.class, hint = PatchingEventSpy.HINT)
public class PatchingEventSpy implements EventSpy {

	public static final String HINT = Properties.PREFIX_ID + "-eventspy-patching";

	@Requirement
	private Logger log;

	@Override
	public void init(Context context) throws Exception {
	}

	@Override
	public void onEvent(Object eventObj) throws Exception {

		if (!(eventObj instanceof PatchingEvent))
			return;

		PatchingEvent event = (PatchingEvent) eventObj;

		switch (event.getType()) {
		case INFO_PATCHING_STARTED:
		case INFO_PATCHING_STOPPED:
			printBox(event);
			break;

		default:
			break;
		}

		handle(event);
	}

	@Override
	public void close() throws Exception {
	}

	private void printBox(PatchingEvent event) {
		//////// [INFO]
		//////// ------------------------------------------------------------------------
		if (event.getType() == Type.INFO_PATCHING_STARTED)
			this.log.info("\033[1m-----------------------< Runes4Maven Extension >------------------------\033[m");
		if (event.getType() == Type.INFO_PATCHING_STOPPED)
			this.log.info("");
	}

	private void handle(PatchingEvent event) {

	}

}
