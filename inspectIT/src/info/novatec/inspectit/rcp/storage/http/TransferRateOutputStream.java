package info.novatec.inspectit.rcp.storage.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Special output stream that reports the bytes sent to the {@link TransferDataMonitor}. Since it is
 * extending the FilterOutputStream all operations are forwarded to the wrapped stream.
 * 
 * @author Ivan Senic
 * 
 */
public class TransferRateOutputStream extends FilterOutputStream {

	/**
	 * Data monitor to report to.
	 */
	private TransferDataMonitor transferDataMonitor;

	/**
	 * Default constructor.
	 * 
	 * @param outputStream
	 *            Stream.
	 * @param transferDataMonitor
	 *            Data monitor to report to.
	 */
	public TransferRateOutputStream(OutputStream outputStream, TransferDataMonitor transferDataMonitor) {
		super(outputStream);
		this.transferDataMonitor = transferDataMonitor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
		markSent(len);
	}

	/**
	 * Marks a sent amount of bytes.
	 * 
	 * @param byteCount
	 *            Byte count.
	 */
	private void markSent(long byteCount) {
		transferDataMonitor.addSample(byteCount);
	}

}
