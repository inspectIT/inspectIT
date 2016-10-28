package rocks.inspectit.shared.all.tracing.data;

import java.sql.Timestamp;
import java.util.Map;

import rocks.inspectit.shared.all.tracing.constants.Tag;

/**
 * Interface for all the spans that we have.
 *
 * @author Ivan Senic
 *
 */
public interface Span {

	/**
	 * Returns the span identification. Must not be <code>null</code>.
	 *
	 * @return Returns the span identification. Must not be <code>null</code>.
	 */
	SpanIdent getSpanIdent();

	/**
	 * Time-stamp when the span started.
	 *
	 * @return Time-stamp when the span started.
	 */
	Timestamp getTimeStamp();

	/**
	 * Duration of the span in milliseconds.
	 *
	 * @return Duration of the span in milliseconds.
	 */
	double getDuration();

	/**
	 * Adds tag to this span.
	 *
	 * @param tag
	 *            {@link Tag}, must not be <code>null</code>.
	 * @param value
	 *            String value, must not be <code>null</code>.
	 * @return Old value associated with same tag.
	 */
	String addTag(Tag tag, String value);

	/**
	 * Adds all tags from the given map to the tags of this span.
	 *
	 * @param otherTags
	 *            Map of tags to add.
	 */
	void addAllTags(Map<Tag, String> otherTags);

	/**
	 * Tags that are giving more information about the span.
	 *
	 * @return Tags that are giving more information about the span.
	 * @see Tag
	 */
	Map<Tag, String> getTags();

	/**
	 * Returns propagation type that defines how reference spans are propagated related to
	 * cross-process propagation.
	 *
	 * @return {@link PropagationType}
	 */
	PropagationType getPropagationType();

	/**
	 * Reference type describes what are time constraints between two spans. Can be Child-Of (caller
	 * wait for callee) or Follow-From (caller does not wait for the callee). More info or the
	 * relationships can be read in the class {@link ReferenceType}.
	 *
	 * @return {@link ReferenceType}
	 */
	ReferenceType getReferenceType();

	/**
	 * Denotes if the this span is caller span.
	 *
	 * @return If it is caller span.
	 */
	boolean isCaller();

	/**
	 * Denotes if the this span is called span. Always opposite of {@link #isCaller()}.
	 *
	 * @return If it is called span.
	 */
	boolean isCalled();

}
