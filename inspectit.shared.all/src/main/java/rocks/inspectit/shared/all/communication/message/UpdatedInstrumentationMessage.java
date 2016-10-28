package rocks.inspectit.shared.all.communication.message;

import java.util.ArrayList;
import java.util.List;

import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;

/**
 * Message to notify the agent about changed {@link InstrumentationDefinition}s.
 *
 * @author Marius Oehler
 *
 */
public class UpdatedInstrumentationMessage implements IAgentMessage<List<InstrumentationDefinition>> {

	/**
	 * List of the updated {@link InstrumentationDefinition}s.
	 */
	private final List<InstrumentationDefinition> updatedInstrumentationDefinitions = new ArrayList<InstrumentationDefinition>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<InstrumentationDefinition> getMessageContent() {
		return updatedInstrumentationDefinitions;
	}
}
