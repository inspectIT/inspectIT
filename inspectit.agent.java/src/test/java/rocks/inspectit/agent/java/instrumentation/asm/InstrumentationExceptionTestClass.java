package rocks.inspectit.agent.java.instrumentation.asm;


/**
 * DO NOT MODIFY THIS CLASS UNLESS YOU KNOW WHAT YOU'RE DOING.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
@SuppressWarnings("PMD")
public class InstrumentationExceptionTestClass {

	public InstrumentationExceptionTestClass() {
	}

	public InstrumentationExceptionTestClass(String message) throws MyTestException {
		try {
			throw new MyTestException(message);
		} catch (MyTestException e) {
		}
	}

	public InstrumentationExceptionTestClass(int i) throws MyTestException {
		throwsAnException();
	}

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
			InstrumentationExceptionTestClass.staticThrowsAnException();
		} catch (MyTestException exception) {
		}
	}

	public static void staticThrowsAnException() throws MyTestException {
		throw new MyTestException();
	}

	public void callsMethodWithExceptionAndTryCatchFinally() {
		try {
			throwsAnException();
		} catch (MyTestException e) {
		} finally {
		}
	}
}
