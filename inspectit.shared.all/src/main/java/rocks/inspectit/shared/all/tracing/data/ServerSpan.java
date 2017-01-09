package rocks.inspectit.shared.all.tracing.data;

/**
 * Our server span.
 *
 * @author Ivan Senic
 *
 */
public class ServerSpan extends AbstractSpan {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 7430959547274725654L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCaller() {
		return false;
	}

}
