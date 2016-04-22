package rocks.inspectit.ui.rcp.storage.util;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import rocks.inspectit.ui.rcp.storage.http.TransferDataMonitor;
import rocks.inspectit.ui.rcp.storage.http.TransferRateOutputStream;

/**
 * Wrapping entity to support upload speed monitoring.
 *
 * @author Ivan Senic
 *
 */
public class UploadHttpEntityWrapper extends HttpEntityWrapper {

	/**
	 * {@link TransferDataMonitor} to pass to the {@link TransferRateOutputStream}.
	 */
	private TransferDataMonitor transferDataMonitor;

	/**
	 * Default constructor.
	 *
	 * @param wrapped
	 *            Entity to be wrapped.
	 * @param transferDataMonitor
	 *            {@link TransferDataMonitor} to pass to the {@link TransferRateOutputStream}.
	 */
	public UploadHttpEntityWrapper(HttpEntity wrapped, TransferDataMonitor transferDataMonitor) {
		super(wrapped);
		this.transferDataMonitor = transferDataMonitor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		OutputStream targetStream = new TransferRateOutputStream(outstream, transferDataMonitor);
		super.writeTo(targetStream);
	};

}