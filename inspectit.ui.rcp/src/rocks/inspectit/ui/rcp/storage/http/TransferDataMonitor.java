package info.novatec.inspectit.rcp.storage.http;

import info.novatec.inspectit.rcp.formatter.NumberFormatter;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.progress.UIJob;

/**
 * Transfer monitor class that collects {@link DataSample}s during the transfer and provides
 * informations about the average transfer rate, total bytes transfered, etc.
 * 
 * @author Ivan Senic
 * 
 */
public class TransferDataMonitor {

	/**
	 * Millis to pass to display the message.
	 */
	private static final long DISPLAY_MESSAGE_RATE = 1000;

	/**
	 * Percentage of file size that will be used if Gzip is used.
	 */
	private static final double GZIP_FILE_SIZE_RATIO = 0.15;

	/**
	 * This value is provided in the CMR by default and represents minimum file size for which GZIp
	 * compression will be used.
	 */
	private static final long MIN_GZIP_FILE_SIZE = 1048576;

	/**
	 * Amount of bytes transfered.
	 */
	private long totalBytesTransfered;

	/**
	 * Time when download started.
	 */
	private long downloadStartTime;

	/**
	 * Sub monitor to report to.
	 */
	private SubMonitor subMonitor;

	/**
	 * Files that have to be downloaded.
	 */
	private Map<String, Long> files;

	/**
	 * If the GZip compression is active.
	 */
	private boolean gzipCompression;

	/**
	 * Amount of finished transfers.
	 */
	private int filesCount = 0;

	/**
	 * Total size that needs to be downloaded.
	 */
	private long totalSize;

	/**
	 * Amount of size we believe in finished files.
	 */
	private long finishedFilesSize;

	/**
	 * Size of the currently downloaded file.
	 */
	private long currentFileSize;

	/**
	 * Amount of bytes that we reported for current file.
	 */
	private long currentFileReal;

	/**
	 * Job for displying the message.
	 */
	private DisplayMessageJob displayMessageJob = new DisplayMessageJob();

	/**
	 * Default constructor. Same as calling
	 * {@link TransferDataMonitor#TransferDataMonitor(SubMonitor, Map, false)}.
	 * 
	 * @param subMonitor
	 *            Monitor to report to.
	 * @param files
	 *            Files to be transfered.
	 */
	public TransferDataMonitor(SubMonitor subMonitor, Map<String, Long> files) {
		this(subMonitor, files, false);
	}

	/**
	 * @param subMonitor
	 *            Monitor to report to.
	 * @param files
	 *            Files to be transfered.
	 * @param gzipCompression
	 *            If GZip compression will be active.
	 */
	public TransferDataMonitor(SubMonitor subMonitor, Map<String, Long> files, boolean gzipCompression) {
		this.subMonitor = subMonitor;
		this.files = files;
		this.gzipCompression = gzipCompression;

		totalSize = 0;
		// try to calculate the total size based on if the gzip is on
		for (Map.Entry<String, Long> entry : files.entrySet()) {
			totalSize += getFileSize(entry.getValue().longValue());
		}

		subMonitor.setWorkRemaining((int) totalSize);
		displayMessageJob.schedule();
	}

	/**
	 * Marks the start of the file download.
	 * 
	 * @param fileName
	 *            Name of the file that is downloaded.
	 */
	public void startTransfer(String fileName) {
		if (0 == filesCount) {
			downloadStartTime = System.currentTimeMillis();
		}

		// reset values for the current file
		currentFileReal = 0;
		currentFileSize = getFileSize(files.get(fileName).longValue());
	}

	/**
	 * Informs that the download of the file has ended.
	 * 
	 * @param fileName
	 *            Name of the file that has been downloaded.
	 */
	public void endTransfer(String fileName) {
		// if the file is smaller than expected (can happen with gzip) add remaining expected size
		// to the submonitor
		filesCount++;
		long leftNotReported = currentFileSize - currentFileReal;
		if (leftNotReported > 0) {
			subMonitor.worked((int) leftNotReported);
		}

		// update the total finised files size
		long originalSize = files.get(fileName).longValue();
		finishedFilesSize += getFileSize(originalSize);

		if (files.size() == filesCount) {
			// when last file is finished we cancel the message job and inform submonitor
			displayMessageJob.cancel();
			subMonitor.subTask("");
			subMonitor.done();
		}
	}

	/**
	 * Adds the sample.
	 * 
	 * @param byteCount
	 *            Bytes transfered.
	 */
	public void addSample(long byteCount) {
		// transfer rate
		totalBytesTransfered += byteCount;

		// reporting stuff
		long reportSize = currentFileSize - currentFileReal;
		// only report if there is some size left
		if (reportSize > 0) {
			reportSize = Math.min(reportSize, byteCount);
			subMonitor.worked((int) reportSize);
		}
		currentFileReal += byteCount;
	}

	/**
	 * Displays the current state on monitor.
	 */
	private void displayMessageOnMonitor() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("File ");
		stringBuilder.append(filesCount + 1);
		stringBuilder.append('/');
		stringBuilder.append(files.size());
		stringBuilder.append(" (");
		stringBuilder.append(NumberFormatter.humanReadableByteCount(totalBytesTransfered));
		if (!gzipCompression) {
			stringBuilder.append(" out of ");
			stringBuilder.append(NumberFormatter.humanReadableByteCount(totalSize));
		}
		stringBuilder.append(" @ ");
		stringBuilder.append(NumberFormatter.humanReadableByteCount((long) getAverageTransferRate()));
		stringBuilder.append("/s) Remaining time: ");
		if (gzipCompression) {
			stringBuilder.append("app. ");
		}
		long quasiBytesLeft = totalSize - finishedFilesSize;
		if (currentFileReal < currentFileSize) {
			quasiBytesLeft -= currentFileReal;
		} else {
			quasiBytesLeft -= currentFileSize;
		}
		// always report at least one sec
		long millisLeft = getMillisLeft(quasiBytesLeft);
		millisLeft += millisLeft % 1000;
		stringBuilder.append(NumberFormatter.humanReadableMillisCount(millisLeft, true));
		subMonitor.subTask(stringBuilder.toString());
	}

	/**
	 * Returns the average transfer rate.
	 * 
	 * @return Returns the average transfer rate.
	 */
	private double getAverageTransferRate() {
		return (double) totalBytesTransfered / ((double) (System.currentTimeMillis() - downloadStartTime) / 1000.0d);
	}

	/**
	 * Returns time left for the given amounts of bytes to be downloaded. Note that this is not
	 * related to the current transfer rate, but to the time since the download started. Thus this
	 * can be used as the information when it is gonna be completely over.
	 * 
	 * @param bytesMore
	 *            Bytes left to be downloaded.
	 * @return Estimated time left in milliseconds.
	 */
	private long getMillisLeft(long bytesMore) {
		return (long) ((bytesMore * 1000.0d) / getAverageTransferRate());
	}

	/**
	 * Returns the size of the file based on if the current download is compressed or not.
	 * 
	 * @param originalFileSize
	 *            Original file size.
	 * @return Returns file size to use.
	 */
	private long getFileSize(long originalFileSize) {
		if (gzipCompression && originalFileSize > MIN_GZIP_FILE_SIZE) {
			return (long) (originalFileSize * GZIP_FILE_SIZE_RATIO);
		} else {
			return originalFileSize;
		}
	}

	/**
	 * Job that will display the message.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class DisplayMessageJob extends UIJob {

		/**
		 * Default constructor.
		 */
		public DisplayMessageJob() {
			super("Display Message");
			setUser(false);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			displayMessageOnMonitor();
			if (!monitor.isCanceled()) {
				schedule(DISPLAY_MESSAGE_RATE);
			}
			return Status.OK_STATUS;
		}

	}

}
