/*
 * Copyright © 2023 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
