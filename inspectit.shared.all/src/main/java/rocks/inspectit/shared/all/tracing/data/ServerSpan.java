package rocks.inspectit.shared.all.tracing.data;

/**
 * The server span is the span that is usually called over RPC or simply created manually. Currently
 * it's the only difference to a client span, but in future we might add different behavior methods,
 * thus these two are separated.
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
