package rocks.inspectit.agent.java.sending;

import java.util.Map;

import rocks.inspectit.agent.java.core.ICoreService;

/**
 * All sending strategies are first initialized via the {@link #init(Map)} method. Afterwards, the
 * sending strategy has to be started manually with {@link #start(ICoreService)}.
 *
 * @author Patrice Bouillet
 *
 */
public interface ISendingStrategy {

	/**
	 * Start the strategy.
	 *
	 * @param coreService
	 *            The core service reference is needed for the strategy to fire the event that the
	 *            core service should send its measurements now.
	 */
	void start(ICoreService coreService);

	/**
	 * Stop the strategy.
	 */
	void stop();

	/**
	 * Initializes the abstract strategy object and stores a reference to an {@link ICoreService}
	 * implementation.
	 *
	 * @param settings
	 *            Settings saved in a {@link Map}. Will be redirected to {@link #initStrategy(Map)}.
	 */
	void init(Map<String, String> settings);

}