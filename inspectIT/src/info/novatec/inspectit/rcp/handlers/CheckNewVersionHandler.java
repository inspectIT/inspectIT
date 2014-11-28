package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.job.CheckNewVersionJob;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Handler for checking the new version. Just start the new {@link CheckNewVersionJob}.
 * 
 * @author Ivan Senic
 * 
 */
public class CheckNewVersionHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job checkNewVersionJob = new CheckNewVersionJob(true);
		checkNewVersionJob.schedule();
		return null;
	}

}
