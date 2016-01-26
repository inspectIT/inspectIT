package rocks.inspectit.agent.java.analyzer;

import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;

/**
 * Our class hash helper that can help in recognizing what classes have been sent to the CMR, what
 * is the instrumentation result for a given class and which class loaders loaded the class.
 *
 * @author Ivan Senic
 */
public interface IClassHashHelper {

	/**
	 * Returns if the class with given hash has been sent to the CMR. Only hashes that are
	 * registered with {@link #registerSent(String)} are considered as sent ones.
	 *
	 * @param hash
	 *            Hash to check
	 *
	 *
	 * @return Returns if the class with given hash has been sent to the CMR.
	 */
	boolean isSent(String hash);

	/**
	 * Registers the class with given hash as being sent to the CMR.
	 * <p>
	 * Sets the instrumentation result for the class with the given hash. Result will be set only if
	 * its not <code>null</code> and {@link InstrumentationDefinition#isEmpty()} is false, otherwise
	 * there is no reason to cache the result.
	 *
	 * @param hash
	 *            Class hash
	 * @param instrumentationResult
	 *            Instrumentation result
	 */
	void register(String hash, InstrumentationDefinition instrumentationResult);

	/**
	 * Returns the {@link InstrumentationDefinition} for the class with given hash if the one was been
	 * set with the {@link #registerInstrumentationResult(String, InstrumentationDefinition)}.
	 *
	 * @param hash
	 *            Class hash
	 * @return {@link InstrumentationDefinition} or <code>null</code> if no result was set for given
	 *         hash.
	 */
	InstrumentationDefinition getInstrumentationResult(String hash);

}
