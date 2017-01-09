package rocks.inspectit.shared.all.tracing.data;

/**
 * The client span is the span that makes a call usually over RPC to another span. Currently it's
 * the only difference to a server span, but in future we might add different behavior methods, thus
 * these two are separated.
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
