package rocks.inspectit.shared.all.tracing.data;

/**
 * Our client span.
 *
 * @author Ivan Senic
 *
 */
public class ClientSpan extends AbstractSpan {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -5415646396600896897L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCaller() {
		return true;
	}

}
