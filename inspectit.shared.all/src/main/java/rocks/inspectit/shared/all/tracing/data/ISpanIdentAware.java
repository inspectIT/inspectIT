package rocks.inspectit.shared.all.tracing.data;

/**
 * Interface for all the classes that can provide span ident. At the moment these are spans and
 * invocations.
 *
 * @author Ivan Senic
 *
 */
public interface ISpanIdentAware {

	/**
	 * Returns span ident.
	 *
	 * @return Returns span ident.
	 */
	SpanIdent getSpanIdent();
}
