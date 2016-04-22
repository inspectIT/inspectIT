package rocks.inspectit.agent.java.connection;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * <b>IMPORTANT:</b> The class code is copied/taken from
 * <a href="http://www.onjava.com/pub/a/onjava/2001/10/17/rmi.html.">O'REILLY onJava.com</a>.
 * Original author is William Grosso. License info can be found
 * <a href="http://www.oreilly.com/terms/">here</a>.
 * 
 * @author William Grosso
 */

public class RetryException extends Exception implements Externalizable {

	/**
	 * The serial version UID of this class.
	 */
	private static final long serialVersionUID = 0L;

	/**
	 * {@inheritDoc}
	 */
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
	}

	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(ObjectOutput output) throws IOException {
	}

}
