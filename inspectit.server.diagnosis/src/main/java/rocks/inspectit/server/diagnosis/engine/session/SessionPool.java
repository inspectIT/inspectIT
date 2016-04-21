package rocks.inspectit.server.diagnosis.engine.session;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.apache.commons.pool.impl.GenericObjectPool;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineConfiguration;
import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineException;

/**
 * Extends GenericObjectPool to enable a parametrized activation of pooled objects.
 *
 * @param <I>
 *            The session input type
 * @param <R>
 *            The expected output type
 * @author Claudio Waldvogel, Alexander Wert
 */
public class SessionPool<I, R> extends GenericObjectPool<Session<I, R>> {

	/**
	 * Default constructor to create a new <code>SessionPool</code>.
	 *
	 * @param engineConfiguration
	 *            The top-level {@link DiagnosisEngineConfiguration}. Must not be null!
	 * @throws DiagnosisEngineException
	 *             If {@link DiagnosisEngineConfiguration} is invalid.
	 * @see DiagnosisEngineConfiguration
	 */
	public SessionPool(DiagnosisEngineConfiguration<I, R> engineConfiguration) throws DiagnosisEngineException {
		this(engineConfiguration, defaultConfig(engineConfiguration));
	}

	/**
	 * Constructor to create a <code>SessionPool</code> with an additional GenericObjectPool.Config.
	 *
	 * @param engineConfiguration
	 *            The DiagnosisEngineConfiguration. Must not be null!
	 * @param poolConfiguration
	 *            The GenericObjectPool.Config. Must not be null!
	 * @throws DiagnosisEngineException
	 *             If {@link DiagnosisEngineConfiguration} is invalid.
	 * @see DiagnosisEngineConfiguration
	 * @see GenericObjectPool.Config
	 */
	public SessionPool(DiagnosisEngineConfiguration<I, R> engineConfiguration, GenericObjectPool.Config poolConfiguration) throws DiagnosisEngineException {
		super(new SessionFactory<>(checkNotNull(engineConfiguration)), checkNotNull(poolConfiguration));
	}

	// -------------------------------------------------------------
	// Methods: Session access
	// -------------------------------------------------------------

	/**
	 * Since we want to activate our Session objects with parameters we need to add an additional
	 * borrowObject method. This method used internally the GenericObjectPool#borrowObject method,
	 * but invokes the parametrized {@link Session#activate(Object, Map<String, ?>)} method to
	 * ensure a proper activation of the {@link Session}.
	 *
	 * @param input
	 *            The input object to analyzed
	 * @param variables
	 *            The session variables valid for this session execution.
	 * @return An activated and ready to use {@link Session} instance.
	 * @see Session
	 * @see GenericObjectPool
	 * @throws Exception
	 *             in case of borrowing the session fails.
	 */
	public Session<I, R> borrowObject(I input, Map<String, ?> variables) throws Exception {
		try {
			return super.borrowObject().activate(input, variables);
		} catch (Exception e) {
			throw new RuntimeException("Failed to borrow object from SessionPool.", e);
		}
	}

	// -------------------------------------------------------------
	// Methods: Internals
	// -------------------------------------------------------------

	/**
	 * Utility method to create a default GenericObjectPool.Config with configuration values from
	 * DiagnosisEngineConfiguration.
	 *
	 * @param configuration
	 *            The top-level DiagnosisEngineConfiguration
	 * @return A new {@link GenericObjectPool.Config} instance.
	 */
	private static GenericObjectPool.Config defaultConfig(DiagnosisEngineConfiguration<?, ?> configuration) {
		GenericObjectPool.Config config = new GenericObjectPool.Config();
		config.whenExhaustedAction = GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION;
		config.maxActive = configuration.getNumSessionWorkers();
		return config;
	}
}
