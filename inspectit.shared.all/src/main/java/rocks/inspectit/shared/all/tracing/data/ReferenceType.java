package rocks.inspectit.shared.all.tracing.data;

/**
 * The reference types that can exists between spans. We took the same concept as opentracing.io
 * (see http://opentracing.io/documentation/pages/spec).
 *
 * @author Ivan Senic
 *
 */
public enum ReferenceType {

	/**
	 * When parent span depends on the child.
	 */
	CHILD_OF,

	/**
	 * When parent does not depend in any way on the child.
	 */
	FOLLOW_FROM;

}
