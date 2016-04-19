/**
 *
 */
package rocks.inspectit.shared.all.exception.enumeration;

import org.apache.commons.lang.WordUtils;

import rocks.inspectit.shared.all.exception.IErrorCode;

/**
 * @author Mario Mann
 *
 */
public enum InvocationSequenceManagementErrorCodeEnum implements IErrorCode {

	/**
	 * Agent does not exists.
	 */
	INVOCATION_SEQUENCE_DOES_NOT_EXIST("The invocation sequence to execute selected operation on does not exist.", "", null);

	/**
	 * Name of the component.
	 */
	private static final String COMPONENT_NAME = "Invocation Sequence Management";

	/**
	 * Description of the error code.
	 */
	private final String description;

	/**
	 * Possible cause(es) for the error.
	 */
	private final String possibleCause;

	/**
	 * Possible solution(s) for the error.
	 */
	private final String possibleSolution;

	/**
	 * {@inheritDoc}
	 */
	public String getComponent() {
		return COMPONENT_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return WordUtils.capitalizeFully(this.toString().replace("_", " ").toLowerCase());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPossibleCause() {
		return possibleCause;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPossibleSolution() {
		return possibleSolution;
	}

	/**
	 * Constructor.
	 *
	 * @param description
	 *            Description of the error code.
	 * @param possibleCause
	 *            Possible cause(es) for the error.
	 * @param possibleSolution
	 *            Possible solution(s) for the error.
	 */
	InvocationSequenceManagementErrorCodeEnum(String description, String possibleCause, String possibleSolution) {
		if (null == description) {
			throw new IllegalArgumentException("Description for the error code must not be null");
		}
		this.description = description;
		this.possibleCause = possibleCause;
		this.possibleSolution = possibleSolution;
	}

}
