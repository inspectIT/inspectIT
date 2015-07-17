package org.eclipse.core.runtime;

/*
 * This class is needed for the overwritten InspectIT class. There are no changes to this file.
 * The other approach would be to include all org.eclipse.core stuff, but this would enlarge
 * this package a lot.
 * 
 * For the build in Eclipse this class is excluded, as in Eclipse running an application
 * does load the eclipse classes and overwritting the Submonitor results in Verifier problems.
 */

//NOCHKALL
public final class SubMonitor {

	/**
	 * May be passed as a flag to newChild. Indicates that the calls to subTask on the child should
	 * be ignored. Without this flag, calling subTask on the child will result in a call to subTask
	 * on its parent.
	 */
	public static final int SUPPRESS_SUBTASK = 0x0001;

	/**
	 * May be passed as a flag to newChild. Indicates that strings passed into beginTask should be
	 * ignored. If this flag is specified, then the progress monitor instance will accept null as
	 * the first argument to beginTask. Without this flag, any string passed to beginTask will
	 * result in a call to setTaskName on the parent.
	 */
	public static final int SUPPRESS_BEGINTASK = 0x0002;

	/**
	 * May be passed as a flag to newChild. Indicates that strings passed into setTaskName should be
	 * ignored. If this string is omitted, then a call to setTaskName on the child will result in a
	 * call to setTaskName on the parent.
	 */
	public static final int SUPPRESS_SETTASKNAME = 0x0004;

	/**
	 * May be passed as a flag to newChild. Indicates that strings passed to setTaskName, subTask,
	 * and beginTask should all be ignored.
	 */
	public static final int SUPPRESS_ALL_LABELS = SUPPRESS_SETTASKNAME | SUPPRESS_BEGINTASK | SUPPRESS_SUBTASK;

	/**
	 * May be passed as a flag to newChild. Indicates that strings passed to setTaskName, subTask,
	 * and beginTask should all be propagated to the parent.
	 */
	public static final int SUPPRESS_NONE = 0;

	/**
	 * Creates a new SubMonitor that will report its progress via the given RootInfo.
	 * 
	 * @param rootInfo
	 *            the root of this progress monitor tree
	 * @param totalWork
	 *            total work to perform on the given progress monitor
	 * @param availableToChildren
	 *            number of ticks allocated for this instance's children
	 * @param flags
	 *            a bitwise combination of the SUPPRESS_* constants
	 */
	private SubMonitor() {
	}

	/**
	 * <p>
	 * Converts an unknown (possibly null) IProgressMonitor into a SubMonitor. It is not necessary
	 * to call done() on the result, but the caller is responsible for calling done() on the
	 * argument. Calls beginTask on the argument.
	 * </p>
	 * 
	 * <p>
	 * This method should generally be called at the beginning of a method that accepts an
	 * IProgressMonitor in order to convert the IProgressMonitor into a SubMonitor.
	 * </p>
	 * 
	 * @param monitor
	 *            monitor to convert to a SubMonitor instance or null. Treats null as a new instance
	 *            of <code>NullProgressMonitor</code>.
	 * @return a SubMonitor instance that adapts the argument
	 */
	public static SubMonitor convert(IProgressMonitor monitor) {
		return convert(monitor, "", 0); //$NON-NLS-1$
	}

	/**
	 * <p>
	 * Converts an unknown (possibly null) IProgressMonitor into a SubMonitor allocated with the
	 * given number of ticks. It is not necessary to call done() on the result, but the caller is
	 * responsible for calling done() on the argument. Calls beginTask on the argument.
	 * </p>
	 * 
	 * <p>
	 * This method should generally be called at the beginning of a method that accepts an
	 * IProgressMonitor in order to convert the IProgressMonitor into a SubMonitor.
	 * </p>
	 * 
	 * @param monitor
	 *            monitor to convert to a SubMonitor instance or null. Treats null as a new instance
	 *            of <code>NullProgressMonitor</code>.
	 * @param work
	 *            number of ticks that will be available in the resulting monitor
	 * @return a SubMonitor instance that adapts the argument
	 */
	public static SubMonitor convert(IProgressMonitor monitor, int work) {
		return convert(monitor, "", work); //$NON-NLS-1$
	}

	/**
	 * <p>
	 * Converts an unknown (possibly null) IProgressMonitor into a SubMonitor allocated with the
	 * given number of ticks. It is not necessary to call done() on the result, but the caller is
	 * responsible for calling done() on the argument. Calls beginTask on the argument.
	 * </p>
	 * 
	 * <p>
	 * This method should generally be called at the beginning of a method that accepts an
	 * IProgressMonitor in order to convert the IProgressMonitor into a SubMonitor.
	 * </p>
	 * 
	 * @param monitor
	 *            to convert into a SubMonitor instance or null. If given a null argument, the
	 *            resulting SubMonitor will not report its progress anywhere.
	 * @param taskName
	 *            user readable name to pass to monitor.beginTask. Never null.
	 * @param work
	 *            initial number of ticks to allocate for children of the SubMonitor
	 * @return a new SubMonitor instance that is a child of the given monitor
	 */
	public static SubMonitor convert(IProgressMonitor monitor, String taskName, int work) {
		return new SubMonitor();
	}

	/**
	 * <p>
	 * Sets the work remaining for this SubMonitor instance. This is the total number of ticks that
	 * may be reported by all subsequent calls to worked(int), newChild(int), etc. This may be
	 * called many times for the same SubMonitor instance. When this method is called, the remaining
	 * space on the progress monitor is redistributed into the given number of ticks.
	 * </p>
	 * 
	 * <p>
	 * It doesn't matter how much progress has already been reported with this SubMonitor instance.
	 * If you call setWorkRemaining(100), you will be able to report 100 more ticks of work before
	 * the progress meter reaches 100%.
	 * </p>
	 * 
	 * @param workRemaining
	 *            total number of remaining ticks
	 * @return the receiver
	 */
	public SubMonitor setWorkRemaining(int workRemaining) {
		return new SubMonitor();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
	 */
	public void setTaskName(String name) {
	}

	/**
	 * Starts a new main task. The string argument is ignored if and only if the SUPPRESS_BEGINTASK
	 * flag has been set on this SubMonitor instance.
	 * 
	 * <p>
	 * This method is equivalent calling setWorkRemaining(...) on the receiver. Unless the
	 * SUPPRESS_BEGINTASK flag is set, this will also be equivalent to calling setTaskName(...) on
	 * the parent.
	 * </p>
	 * 
	 * @param name
	 *            new main task name
	 * @param totalWork
	 *            number of ticks to allocate
	 * 
	 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
	 */
	public void beginTask(String name, int totalWork) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
	 */
	public void subTask(String name) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
	 */
	public void worked(int work) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(boolean b) {
	}

	/**
	 * <p>
	 * Creates a sub progress monitor that will consume the given number of ticks from the receiver.
	 * It is not necessary to call <code>beginTask</code> or <code>done</code> on the result.
	 * However, the resulting progress monitor will not report any work after the first call to
	 * done() or before ticks are allocated. Ticks may be allocated by calling beginTask or
	 * setWorkRemaining.
	 * </p>
	 * 
	 * <p>
	 * Each SubMonitor only has one active child at a time. Each time newChild() is called, the
	 * result becomes the new active child and any unused progress from the previously-active child
	 * is consumed.
	 * </p>
	 * 
	 * <p>
	 * This is property makes it unnecessary to call done() on a SubMonitor instance, since child
	 * monitors are automatically cleaned up the next time the parent is touched.
	 * </p>
	 * 
	 * <code><pre> 
	 *      ////////////////////////////////////////////////////////////////////////////
	 *      // Example 1: Typical usage of newChild
	 *      void myMethod(IProgressMonitor parent) {
	 *          SubMonitor progress = SubMonitor.convert(parent, 100); 
	 *          doSomething(progress.newChild(50));
	 *          doSomethingElse(progress.newChild(50));
	 *      }
	 *      
	 *      ////////////////////////////////////////////////////////////////////////////
	 *      // Example 2: Demonstrates the function of active children. Creating children
	 *      // is sufficient to smoothly report progress, even if worked(...) and done()
	 *      // are never called.
	 *      void myMethod(IProgressMonitor parent) {
	 *          SubMonitor progress = SubMonitor.convert(parent, 100);
	 *          
	 *          for (int i = 0; i < 100; i++) {
	 *              // Creating the next child monitor will clean up the previous one,
	 *              // causing progress to be reported smoothly even if we don't do anything
	 *              // with the monitors we create
	 *          	progress.newChild(1);
	 *          }
	 *      }
	 *      
	 *      ////////////////////////////////////////////////////////////////////////////
	 *      // Example 3: Demonstrates a common anti-pattern
	 *      void wrongMethod(IProgressMonitor parent) {
	 *          SubMonitor progress = SubMonitor.convert(parent, 100);
	 *          
	 *          // WRONG WAY: Won't have the intended effect, as only one of these progress
	 *          // monitors may be active at a time and the other will report no progress.
	 *          callMethod(progress.newChild(50), computeValue(progress.newChild(50)));
	 *      }
	 *      
	 *      void rightMethod(IProgressMonitor parent) {
	 *          SubMonitor progress = SubMonitor.convert(parent, 100);
	 *          
	 *          // RIGHT WAY: Break up method calls so that only one SubMonitor is in use at a time.
	 *          Object someValue = computeValue(progress.newChild(50));
	 *          callMethod(progress.newChild(50), someValue);
	 *      }
	 * </pre></code>
	 * 
	 * @param totalWork
	 *            number of ticks to consume from the receiver
	 * @return new sub progress monitor that may be used in place of a new SubMonitor
	 */
	public SubMonitor newChild(int totalWork) {
		return newChild(totalWork, SUPPRESS_BEGINTASK);
	}

	/**
	 * <p>
	 * Creates a sub progress monitor that will consume the given number of ticks from the receiver.
	 * It is not necessary to call <code>beginTask</code> or <code>done</code> on the result.
	 * However, the resulting progress monitor will not report any work after the first call to
	 * done() or before ticks are allocated. Ticks may be allocated by calling beginTask or
	 * setWorkRemaining.
	 * </p>
	 * 
	 * <p>
	 * Each SubMonitor only has one active child at a time. Each time newChild() is called, the
	 * result becomes the new active child and any unused progress from the previously-active child
	 * is consumed.
	 * </p>
	 * 
	 * <p>
	 * This is property makes it unnecessary to call done() on a SubMonitor instance, since child
	 * monitors are automatically cleaned up the next time the parent is touched.
	 * </p>
	 * 
	 * <code><pre> 
	 *      ////////////////////////////////////////////////////////////////////////////
	 *      // Example 1: Typical usage of newChild
	 *      void myMethod(IProgressMonitor parent) {
	 *          SubMonitor progress = SubMonitor.convert(parent, 100); 
	 *          doSomething(progress.newChild(50));
	 *          doSomethingElse(progress.newChild(50));
	 *      }
	 *      
	 *      ////////////////////////////////////////////////////////////////////////////
	 *      // Example 2: Demonstrates the function of active children. Creating children
	 *      // is sufficient to smoothly report progress, even if worked(...) and done()
	 *      // are never called.
	 *      void myMethod(IProgressMonitor parent) {
	 *          SubMonitor progress = SubMonitor.convert(parent, 100);
	 *          
	 *          for (int i = 0; i < 100; i++) {
	 *              // Creating the next child monitor will clean up the previous one,
	 *              // causing progress to be reported smoothly even if we don't do anything
	 *              // with the monitors we create
	 *          	progress.newChild(1);
	 *          }
	 *      }
	 *      
	 *      ////////////////////////////////////////////////////////////////////////////
	 *      // Example 3: Demonstrates a common anti-pattern
	 *      void wrongMethod(IProgressMonitor parent) {
	 *          SubMonitor progress = SubMonitor.convert(parent, 100);
	 *          
	 *          // WRONG WAY: Won't have the intended effect, as only one of these progress
	 *          // monitors may be active at a time and the other will report no progress.
	 *          callMethod(progress.newChild(50), computeValue(progress.newChild(50)));
	 *      }
	 *      
	 *      void rightMethod(IProgressMonitor parent) {
	 *          SubMonitor progress = SubMonitor.convert(parent, 100);
	 *          
	 *          // RIGHT WAY: Break up method calls so that only one SubMonitor is in use at a time.
	 *          Object someValue = computeValue(progress.newChild(50));
	 *          callMethod(progress.newChild(50), someValue);
	 *      }
	 * </pre></code>
	 * 
	 * @param totalWork
	 *            number of ticks to consume from the receiver
	 * @return new sub progress monitor that may be used in place of a new SubMonitor
	 */
	public SubMonitor newChild(int totalWork, int suppressFlags) {
		return new SubMonitor();
	}

	protected static boolean eq(Object o1, Object o2) {
		if (o1 == null)
			return (o2 == null);
		if (o2 == null)
			return false;
		return o1.equals(o2);
	}
}
