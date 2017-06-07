package rocks.inspectit.ui.rcp.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * A progress dialog which can be used to run a task in background using a
 * {@link ProgressMonitorDialog} and returning a result.
 *
 * @param <E>
 *            the type of the result object
 * @author Marius Oehler
 *
 */
public abstract class ProgressDialog<E> {

	/**
	 * Execution was successful.
	 */
	public static final int OK = 0;

	/**
	 * Execution was not successful.
	 */
	public static final int FAILURE = 1;

	/**
	 * Execution was not successful due to an internal exception.
	 */
	public static final int INTERNAL_FAILURE = 2;

	/**
	 * The exit code.
	 */
	private int exitCode = -1;

	/**
	 * The returned object.
	 */
	private E result;

	/**
	 * Exception which has been thrown.
	 */
	private Exception thrownException = null;

	/**
	 * The task name.
	 */
	private String taskName;

	/**
	 * The amount of work to do. Needed for the progress bar.
	 */
	private int totalWork;

	/**
	 * Constructor.
	 *
	 * @param taskName
	 *            the task name
	 * @param totalWork
	 *            the amount of work to do. Needed for the progress bar
	 */
	public ProgressDialog(String taskName, int totalWork) {
		this.taskName = taskName;
		this.totalWork = totalWork;
	}

	/**
	 * The actual work.
	 *
	 * @param monitor
	 *            the progress monitor
	 * @return the object which can be accessed via {@link #getResult()}
	 * @throws Exception
	 *             a exception thrown by inspectIT
	 */
	public abstract E execute(IProgressMonitor monitor) throws Exception;

	/**
	 * Gets {@link #exitCode}.
	 *
	 * @return {@link #exitCode}
	 */
	public int getExitCode() {
		return this.exitCode;
	}

	/**
	 * Sets {@link #exitCode}.
	 *
	 * @param exitCode
	 *            New value for {@link #exitCode}
	 */
	protected void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}

	/**
	 * Gets {@link #result}.
	 *
	 * @return {@link #result}
	 */
	public E getResult() {
		return this.result;
	}

	/**
	 * Gets {@link #thrownException}.
	 *
	 * @return {@link #thrownException}
	 */
	public Exception getThrownException() {
		return this.thrownException;
	}

	/**
	 * Starts the dialog and executes the {@link #execute(IProgressMonitor)} method. See
	 * {@link ProgressMonitorDialog#run(boolean, boolean, IRunnableWithProgress)} for a detailed
	 * description of the parameter.
	 *
	 * @param fork
	 *            true if the runnable should be run in a separate thread, and false to run in the
	 *            same thread
	 * @param cancelable
	 *            true to enable the cancelation, and false to make the operation uncancellable
	 */
	public void start(boolean fork, boolean cancelable) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(shell);

		try {
			monitorDialog.run(fork, cancelable, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(taskName, totalWork);

					try {
						result = execute(monitor);

						if (exitCode == -1) {
							setExitCode(OK);
						}
					} catch (Exception e) {
						if (exitCode == -1) {
							setExitCode(FAILURE);
						}
						thrownException = e;
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			setExitCode(INTERNAL_FAILURE);
			thrownException = e;
		}
	}

	/**
	 * Returns <code>true</code> if the execution has been finished and the return code equals
	 * {@link ProgressDialog#OK}.
	 *
	 * @return <code>true</code> if the execution was successful
	 */
	public boolean wasSuccessful() {
		return exitCode == OK;
	}
}
