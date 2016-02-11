package info.novatec.inspectit.util;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * The {@link MessageFormatFormatter} class is used to format the log output of the standard
 * <code>java.util.logging</code> package. To activate this formatter, edit the
 * <b>logging.properties</b> file in the <b>lib</b> folder of the Java installation or pass a new
 * Java Argument named <code>-Djava.util.logging.config.file</code> in the start command line.
 * 
 * @author Patrice Bouillet
 * 
 */
public class MessageFormatFormatter extends Formatter {

	/**
	 * The message format object which defines the template for the output if no {@link Throwable}
	 * object is contained in the {@link LogRecord}.
	 */
	private static final MessageFormat MESSAGE_FORMAT = new MessageFormat("{0,date,HH:mm:ss,sss} {1} {2} - {3} \n");

	/**
	 * The message format object which defines the template for the output if a {@link Throwable}
	 * object is contained in the {@link LogRecord}.
	 */
	private static final MessageFormat MESSAGE_FORMAT_THROWN = new MessageFormat("{0,date,HH:mm:ss,sss} {1} {2} - {3} \n {4} \n");

	/**
	 * {@inheritDoc}
	 */
	public String format(LogRecord record) {
		Object[] arguments = new Object[5];
		arguments[0] = new Date(record.getMillis());
		arguments[1] = record.getLevel();
		String[] nameSplit = record.getSourceClassName().split("\\.");
		arguments[2] = nameSplit[nameSplit.length - 1];
		arguments[3] = record.getMessage();
		if (null != record.getThrown()) {
			arguments[4] = record.getThrown().toString();
			return MESSAGE_FORMAT_THROWN.format(arguments);
		} else {
			return MESSAGE_FORMAT.format(arguments);
		}
	}

}
