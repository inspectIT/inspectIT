package info.novatec.inspectit.agent.analyzer.classes;

/**
 * DO NOT MODIFY THIS CLASS UNLESS YOU KNOW WHAT YOU'RE DOING.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
@SuppressWarnings("PMD")
public class ExceptionTestClass {

	public void throwsAndHandlesException() {
		try {
			throw new MyTestException();
		} catch (MyTestException e) {
		}
	}

	public void createsExceptionObject() {
		new MyTestException();
	}

	public void callsMethodWithException() {
		try {
			throwsAnException();
		} catch (MyTestException exception) {
		}
	}

	public void throwsAnException() throws MyTestException {
		throw new MyTestException();
	}

	public static void callsStaticMethodWithException() {
		try {
			ExceptionTestClass.staticThrowsAnException();
		} catch (MyTestException exception) {
		}
	}

	public static void staticThrowsAnException() throws MyTestException {
		throw new MyTestException();
	}

	public void constructorThrowsAnException() {
		try {
			new ExceptionalTestClass("bla");
		} catch (MyTestException e) {
		}
	}

	public void callsMethodWithExceptionAndTryCatchFinally() {
		try {
			throwsAnException();
		} catch (MyTestException e) {
		} finally {
		}
	}
}
