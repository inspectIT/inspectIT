package info.novatec.inspectit.rcp.storage.util;

import info.novatec.inspectit.rcp.storage.http.TransferDataMonitor;
import info.novatec.inspectit.rcp.storage.http.TransferRateInputStream;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

/**
 * Wrapping entity to support download speed monitoring.
 * 
 * @author Ivan Senic
 * 
 */
public class DownloadHttpEntityWrapper extends HttpEntityWrapper {

	/**
	 * {@link TransferDataMonitor} to pass to the {@link TransferRateInputStream}.
	 */
	private TransferDataMonitor transferDataMonitor;

	/**
	 * Default constructor.
	 * 
	 * @param wrapped
	 *            Entity to be wrapped.
	 * @param transferDataMonitor
	 *            {@link TransferDataMonitor} to pass to the {@link TransferRateInputStream}.
	 */
	public DownloadHttpEntityWrapper(HttpEntity wrapped, TransferDataMonitor transferDataMonitor) {
		super(wrapped);
		this.transferDataMonitor = transferDataMonitor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getContent() throws IOException {
		InputStream wrappedin = wrappedEntity.getContent();
		return new TransferRateInputStream(wrappedin, transferDataMonitor);
	}

}