package info.novatec.inspectit.agent.analyzer;

import info.novatec.inspectit.instrumentation.config.impl.InstrumentationResult;

import java.util.Collection;

/**
 * Our class hash helper that can help in recognizing what classes have been sent to the CMR, what
 * is the instrumentation result for a given class and which class loaders loaded the class.
 * 
 * @author Ivan Senic
 * 
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
	 * its not <code>null</code> and {@link InstrumentationResult#isEmpty()} is false, otherwise
	 * there is no reason to cache the result.
	 * 
	 * @param hash
	 *            Class hash
	 * @param instrumentationResult
	 *            Instrumentation result
	 */
	void registerSent(String hash, InstrumentationResult instrumentationResult);

	/**
	 * Returns the {@link InstrumentationResult} for the class with given hash if the one was been
	 * set with the {@link #registerInstrumentationResult(String, InstrumentationResult)}.
	 * 
	 * @param hash
	 *            Class hash
	 * @return {@link InstrumentationResult} or <code>null</code> if no result was set for given
	 *         hash.
	 */
	InstrumentationResult getInstrumentationResult(String hash);

	/**
	 * Adds the given class loader to the list of class loaders that the class with given hash has
	 * been loaded with.
	 * 
	 * @param hash
	 *            Class hash.
	 * @param classLoader
	 *            Class loader that loaded class with given hash.
	 */
	void registerLoaded(String hash, ClassLoader classLoader);

	/**
	 * Returns all class loaders that loaded the given class and reported it via
	 * {@link #registerLoaded(String, ClassLoader)}.
	 * 
	 * @param hash
	 *            Class hash
	 * @return Collection of class loaders.
	 */
	Collection<ClassLoader> getClassLoaders(String hash);
}
