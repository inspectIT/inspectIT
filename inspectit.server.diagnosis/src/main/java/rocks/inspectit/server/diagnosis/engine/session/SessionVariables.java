package rocks.inspectit.server.diagnosis.engine.session;

import java.util.HashMap;
import java.util.Map;

/**
 * Container representing session variable in a key-value pair manner.
 *
 * @author Claudio Waldvogel
 */
public class SessionVariables extends HashMap<String, Object> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 4674382247068252913L;

	/**
	 * Empty SessionVariables to be used as default.
	 */
	public static final SessionVariables EMPTY_VARIABLES = new UnmodifiableSessionVariables();

	/**
	 * Unmodifiable SessionVariables.
	 *
	 * @author Alexander Wert
	 *
	 */
	private static class UnmodifiableSessionVariables extends SessionVariables {

		/**
		 *
		 */
		private static final long serialVersionUID = -152163586550022602L;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object put(String key, Object value) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object remove(Object key) {
			throw new UnsupportedOperationException();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void putAll(Map<? extends String, ? extends Object> m) {
			throw new UnsupportedOperationException();

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
	}
}
