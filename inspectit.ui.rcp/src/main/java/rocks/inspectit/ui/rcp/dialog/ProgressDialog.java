package rocks.inspectit.ui.rcp.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import rocks.inspectit.shared.all.exception.BusinessException;

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
	public static final int FAIL = 1;

	/**
	 * The exit code.
	 */
	private int exitCode = -1;

	/**
	 * The returned object.
	 */
	private E result;

	/**
	 * Business exception which has been thrown.
	 */
	private BusinessException thrownException = null;

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
	 * @throws BusinessException
	 *             a exception thrown by inspectIT
	 */
	public abstract E execute(IProgressMonitor monitor) throws BusinessException;

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
	 * Starts the dialog and executes the {@link #execute(IProgressMonitor)} method. See
	 * {@link ProgressMonitorDialog#run(boolean, boolean, IRunnableWithProgress)} for a detailed
	 * description of the parameter.
	 *
	 * @param fork
	 *            true if the runnable should be run in a separate thread, and false to run in the
	 *            same thread
	 * @param cancelable
	 *            true to enable the cancelation, and false to make the operation uncancellable
	 * @throws InvocationTargetException
	 *             wraps any exception or error which occurs while running the runnable
	 * @throws InterruptedException
	 *             propagated by the context if the runnable acknowledges cancelation by throwing
	 *             this exception. This should not be thrown if cancelable is false
	 * @throws BusinessException
	 *             a exception thrown by inspectIT
	 */
	public void start(boolean fork, boolean cancelable) throws InvocationTargetException, InterruptedException, BusinessException {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(shell);

		monitorDialog.run(fork, cancelable, new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(taskName, totalWork);

				try {
					result = execute(monitor);

					if (exitCode == -1) {
						setExitCode(OK);
					}
				} catch (BusinessException e) {
					if (exitCode == -1) {
						setExitCode(FAIL);
					}
					thrownException = e;
				} finally {
					monitor.done();
				}
			}
		});

		if (thrownException != null) {
			throw thrownException;
		}
	}
}
