package net.runeduniverse.tools.maven.r4m.eventspy.api;

import java.util.Collections;
import java.util.Map;

public interface MessagePatchingEvent extends PatchingEvent {

	public Map<String, CharSequence> getMessage();

	public CharSequence getMessage(String key);

	public static MessageEvent createInfoEvent(Type type, Map<String, CharSequence> message) {
		return new MessageEvent(type, null, message);
	}

	public static MessageEvent createErrorEvent(Type type, Exception exception, Map<String, CharSequence> message) {
		return new MessageEvent(type, exception, message);
	}

	public class MessageEvent extends PatchingEvent.BasicEvent implements MessagePatchingEvent {

		protected Map<String, CharSequence> message = null;

		public MessageEvent(Type type, Exception exception, Map<String, CharSequence> message) {
			super(type, exception);
			this.message = message;
		}

		public MessageEvent readonly() {
			if (this.message == null)
				return this;
			this.message = Collections.unmodifiableMap(this.message);
			return this;
		}

		@Override
		public Map<String, CharSequence> getMessage() {
			return this.message;
		}

		@Override
		public CharSequence getMessage(String key) {
			if (this.message == null)
				return null;
			return this.message.get(key);
		}

	}

}
