/*
 * Copyright © 2026 VenaNocta (venanocta@gmail.com)
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
package net.runeduniverse.tools.maven.r4m.pem.converter;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import net.runeduniverse.tools.maven.r4m.pem.converter.api.DataFactory;
import net.runeduniverse.tools.maven.r4m.pem.model.DataEntry;
import net.runeduniverse.tools.maven.r4m.pem.model.Execution;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionRestriction;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionSource;
import net.runeduniverse.tools.maven.r4m.pem.model.ExecutionTrigger;
import net.runeduniverse.tools.maven.r4m.pem.model.Lifecycle;

import static net.runeduniverse.lib.utils.common.StringUtils.isBlank;

@Component(role = DataFactory.class, hint = Execution.HINT)
public class ExecutionDataFactory extends ADataFactory {

	@Override
	public DataEntry createEntry(final PlexusConfiguration cnf) {
		if (!Execution.HINT.equals(cnf.getName()))
			return null;

		final String id = getAttributeAsId(cnf, "id");
		final ExecutionSource source = ExecutionSource.create(getAttributeAsId(cnf, "source"));
		if (isBlank(id) || source == null)
			return null;

		final Execution execution = new Execution(id, source);

		parseInherited(execution, cnf.getChild("inherited", false));
		parseRestrictions(execution, cnf.getChild("restrictions", false));
		parseTriggers(execution, cnf.getChild("triggers", false));
		parseLifecycles(execution, cnf.getChild("lifecycles", false));

		return execution;
	}

	protected boolean parseInherited(final Execution exec, final PlexusConfiguration inheritedNode) {
		if (inheritedNode == null)
			return false;
		final Boolean inherited = getValueAsBoolean(inheritedNode, null);
		if (inherited == null)
			return false;
		exec.setInherited(inherited);
		return true;
	}

	protected boolean parseRestrictions(final Execution exec, final PlexusConfiguration nodeList) {
		if (nodeList == null || nodeList.getChildCount() == 0)
			return false;

		final PlexusConfiguration triggerNodes[] = nodeList.getChildren();
		if (triggerNodes.length > 0) {
			for (PlexusConfiguration triggerNode : triggerNodes) {
				final DataEntry entry = convertEntry(ExecutionRestriction.CONTEXT, triggerNode);
				if (entry instanceof ExecutionRestriction)
					exec.addRestriction((ExecutionRestriction) entry);
			}
		}
		return true;
	}

	protected boolean parseTriggers(final Execution exec, final PlexusConfiguration nodeList) {
		if (nodeList == null || nodeList.getChildCount() == 0)
			return false;

		final PlexusConfiguration triggerNodes[] = nodeList.getChildren();
		if (triggerNodes.length > 0) {
			for (PlexusConfiguration triggerNode : triggerNodes) {
				final String name = triggerNode.getName();
				switch (name) {
				case "on-call":
					// DO NOTING
					// every execution is active when called
					// except when flagged with <never/>
					break;
				case "always":
					exec.setAlwaysActive(true);
					break;
				case "default":
					exec.setDefaultActive(true);
					break;
				case "never":
					exec.setNeverActive(true);
					break;

				default:
					final DataEntry entry = convertEntry(ExecutionTrigger.CONTEXT, triggerNode);
					if (entry instanceof ExecutionTrigger)
						exec.addTrigger((ExecutionTrigger) entry);
					break;
				}
			}
		}
		return true;
	}

	protected boolean parseLifecycles(final Execution exec, final PlexusConfiguration nodeList) {
		if (nodeList == null || nodeList.getChildCount() == 0)
			return false;

		final PlexusConfiguration lifecycleNodes[] = nodeList.getChildren("lifecycle");
		if (lifecycleNodes.length > 0) {
			for (PlexusConfiguration lifecycleNode : lifecycleNodes) {
				final DataEntry entry = convertEntry(lifecycleNode);
				if (entry instanceof Lifecycle)
					exec.putLifecycle((Lifecycle) entry);
			}
		}
		return true;
	}
}
