package info.novatec.inspectit.jmeter.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.jmeter.samplers.SampleResult;

import com.thoughtworks.xstream.XStream;

/**
 * Provides an easier interface to work with {@code SampleResult} objects and allows to integrate
 * <code>Object</code>s as response code. This class will transform these objects to xml.
 * 
 * @author Stefan Siegl
 */
public final class ResultService {

	/** The <code>SampleResult</code> that will be returned. */
	private SampleResult result = new SampleResult();

	/** The XStream instance. */
	XStream xStream = XStreamFactory.getXStream();

	/**
	 * Constructor.
	 */
	private ResultService() {
	}

	/**
	 * Factory.
	 * 
	 * @return new Instance.
	 */
	public static ResultService newInstance() {
		return new ResultService();
	}

	/**
	 * Start the measurement.
	 */
	public void start() {
		result.sampleStart();
	}

	/**
	 * stop the measurement and set the sample to be successful.
	 */
	public void success() {
		result.sampleEnd();
		result.setSuccessful(true);
	}

	/**
	 * Stops the measurement and fails the test run.
	 * 
	 * @param e
	 *            the Exception that made this run fail.
	 * @return the SampleResult
	 */
	@SuppressWarnings("deprecation")
	public SampleResult fail(Throwable e) {
		result.sampleEnd();
		result.setSuccessful(false);
		result.setResponseMessage(e.getMessage());

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		sw.toString(); // stack trace as a string
		result.setResponseData(sw.toString());
		return result;
	}

	/**
	 * Uses the given <code>Object</code> as result of this test run. This object is converted to a
	 * XML representation.
	 * 
	 * @param result
	 *            the result of the test run execution.
	 */
	public void setResult(Object result) {
		if (null == result) {
			this.result.setResponseData("no response data", "UTF-8");
		} else {
			this.result.setResponseData(xStream.toXML(result), "UTF-8");
		}
	}

	/**
	 * Returns the result.
	 * 
	 * @return the result.
	 */
	public SampleResult getResult() {
		return result;
	}
}
