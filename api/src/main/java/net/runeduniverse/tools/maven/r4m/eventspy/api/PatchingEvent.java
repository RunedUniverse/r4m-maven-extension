package net.runeduniverse.tools.maven.r4m.eventspy.api;

public interface PatchingEvent {

	public enum Type {
		INFO_PATCHING_STARTED, INFO_PATCHING_STOPPED, INFO_PATCHING_ABORTED,//
	}

	public Type getType();

	public Exception getException();

	public static PatchingEvent createBasicInfoEvent(Type type) {
		return new BasicEvent(type, null);
	}

	public static PatchingEvent createBasicErrorEvent(Type type, Exception exception) {
		return new BasicEvent(type, exception);
	}

	public class BasicEvent implements PatchingEvent {

		protected Type type = null;
		protected Exception exception = null;

		public BasicEvent(Type type, Exception exception) {
			this.type = type;
			this.exception = exception;
		}

		@Override
		public Type getType() {
			return this.type;
		}

		@Override
		public Exception getException() {
			return this.exception;
		}

	}

}
