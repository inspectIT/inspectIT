package info.novatec.inspectit.exception;

/**
 * General interface for the error code.
 * 
 * @author Ivan Senic
 * 
 */
public interface IErrorCode {

	/**
	 * Returns the name of the component where the error occurred.
	 * 
	 * @return Returns the name of the component where the error occurred.
	 */
	String getComponent();

	/**
	 * Returns general name of the error.
	 * 
	 * @return Returns general name of the error.
	 */
	String getName();

	/**
	 * Returns more detailed description of the error.
	 * 
	 * @return Returns more detailed description of the error.
	 */
	String getDescription();

	/**
	 * Returns the possible cause(es) for this error if one exist.
	 * 
	 * @return Returns the possible cause(es) for this error if one exist.
	 */
	String getPossibleCause();

	/**
	 * Returns the possible solution(s) for this error if one exist.
	 * 
	 * @return Returns the possible solution(s) for this error if one exist.
	 */
	String getPossibleSolution();

}
