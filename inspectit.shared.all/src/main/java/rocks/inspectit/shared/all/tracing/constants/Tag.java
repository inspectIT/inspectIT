package rocks.inspectit.shared.all.tracing.constants;

/**
 * Tags that can be used for tracing purposes.
 *
 * @author Ivan Senic
 *
 */
public interface Tag {

	/**
	 * Returns name of the tag.
	 *
	 * @return Returns name of the tag.
	 */
	String getName();


	/**
	 * Tags for the HTTP remote tracing.
	 *
	 * @author Ivan Senic
	 *
	 */
	enum Http implements Tag {

		/**
		 * Request URL.
		 */
		URL,

		/**
		 * Response status.
		 */
		STATUS;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getName() {
			switch (this) {
			case URL:
				return "http.url";
			case STATUS:
				return "http.status";
			default:
				return null;
			}
		}
	}

	/**
	 * Tags for the JMS remote tracing.
	 *
	 * @author Ivan Senic
	 *
	 */
	enum Jms implements Tag {

		/**
		 * JMS message id.
		 */
		MESSAGE_ID,

		/**
		 * JMS message destination.
		 */
		MESSAGE_DESTINATION;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getName() {
			switch (this) {
			case MESSAGE_ID:
				return "jms.message.id";
			case MESSAGE_DESTINATION:
				return "jms.message.destination";
			default:
				return null;
			}
		}
	}
}
