package rocks.inspectit.agent.java.hooking;

/**
 * The hook dispatcher interface defines methods to add method and constructor mappings and methods
 * to dispatch the calls from the instrumented methods in the target application.
 *
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 *
 */
public interface IHookDispatcher {

	/**
	 * Dispatches the 'before' method statement.
	 *
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class or the class itself.
	 * @param parameters
	 *            The parameters of the method.
	 */
	void dispatchMethodBeforeBody(long id, Object object, Object[] parameters);

	/**
	 * Dispatches the first 'after' method statement.
	 *
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class or the class itself.
	 * @param parameters
	 *            The parameters of the method.
	 * @param returnValue
	 *            The return value of the method.
	 */
	void dispatchFirstMethodAfterBody(long id, Object object, Object[] parameters, Object returnValue);

	/**
	 * Dispatches the second 'after' method statement.
	 *
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class or the class itself.
	 * @param parameters
	 *            The parameters of the method.
	 * @param returnValue
	 *            The return value of the method.
	 */
	void dispatchSecondMethodAfterBody(long id, Object object, Object[] parameters, Object returnValue);

	/**
	 * Dispatches the 'addCatch' statement of a method.
	 *
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class.
	 * @param parameters
	 *            The parameters of the method.
	 * @param exceptionObject
	 *            The instance of the {@link Throwable} object.
	 */
	void dispatchOnThrowInBody(long id, Object object, Object[] parameters, Object exceptionObject);

	/**
	 * Dispatches the handler of a {@link Throwable}.
	 *
	 * @param id
	 *            The id of the method where the {@link Throwable} is handled.
	 * @param exceptionObject
	 *            The instance of the {@link Throwable} object.
	 */
	void dispatchBeforeCatch(long id, Object exceptionObject);

	/**
	 * Dispatches the 'addCatch' statement of a constructor.
	 *
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class.
	 * @param parameters
	 *            The parameters of the constructor.
	 * @param exceptionObject
	 *            The instance of the {@link Throwable} object.
	 */
	void dispatchConstructorOnThrowInBody(long id, Object object, Object[] parameters, Object exceptionObject);

	/**
	 * Dispatches the handler of a {@link Throwable}.
	 *
	 * @param id
	 *            The id of the constructor where the {@link Throwable} is handled.
	 * @param exceptionObject
	 *            The instance of the {@link Throwable} object.
	 */
	void dispatchConstructorBeforeCatch(long id, Object exceptionObject);

	/**
	 * Dispatches the 'before' constructor statement.
	 *
	 * @param id
	 *            The id of the method.
	 * @param parameters
	 *            The parameters of the method.
	 */
	void dispatchConstructorBeforeBody(long id, Object[] parameters);

	/**
	 * Dispatches the 'after' constructor statement.
	 *
	 * @param id
	 *            The id of the method.
	 * @param object
	 *            The instance of the class or the class itself.
	 * @param parameters
	 *            The parameters of the method.
	 */
	void dispatchConstructorAfterBody(long id, Object object, Object[] parameters);

}