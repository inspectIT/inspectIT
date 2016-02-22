package rocks.inspectit.shared.all.exception;

/**
 * General interface for the error code.
 * 
 * @author Ivan Senic
 * 
 */
public interface IErrorCode {

	/**
	 * @return Returns the name of the component where the error occurred.
	 */
	String getComponent();

	/**
	 * @return Returns general name of the error.
	 */
	String getName();

	/**
	 * @return Returns more detailed description of the error.
	 */
	String getDescription();

	/**
	 * @return Returns the possible cause(es) for this error if one exist.
	 */
	String getPossibleCause();

	/**
	 * @return Returns the possible solution(s) for this error if one exist.
	 */
	String getPossibleSolution();

}
