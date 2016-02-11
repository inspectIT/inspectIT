package info.novatec.inspectit.rcp.storage.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Special input stream that reports the bytes received to the {@link TransferDataMonitor}. Since it
 * is extending the FilterInputStream all operations are forwarded to the wrapped stream.
 * 
 * @author Ivan Senic
 * 
 */
public class TransferRateInputStream extends FilterInputStream {

	/**
	 * Data monitor to report to.
	 */
	private TransferDataMonitor transferDataMonitor;

	/**
	 * Default constructor.
	 * 
	 * @param inputStream
	 *            Stream.
	 * @param transferDataMonitor
	 *            {@link TransferDataMonitor} to report to.
	 */
	public TransferRateInputStream(InputStream inputStream, TransferDataMonitor transferDataMonitor) {
		super(inputStream);
		this.transferDataMonitor = transferDataMonitor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		int b = super.read();
		if (b >= 0) {
			markReceived(b);
		}
		return b;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] data, int off, int len) throws IOException {
		int cnt = super.read(data, off, len);
		if (cnt >= 0) {
			markReceived(cnt);
		}
		return cnt;
	}

	/**
	 * Marks a received amount of bytes.
	 * 
	 * @param byteCount
	 *            Byte count.
	 */
	private void markReceived(long byteCount) {
		transferDataMonitor.addSample(byteCount);
	}

}
