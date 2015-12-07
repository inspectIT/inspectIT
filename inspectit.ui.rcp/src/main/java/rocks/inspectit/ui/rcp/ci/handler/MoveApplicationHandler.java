package rocks.inspectit.ui.rcp.ci.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;

import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.provider.IApplicationProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Move {@link ApplicationDefinition} handler.
 *
 * @author Alexander Wert
 *
 */
public class MoveApplicationHandler extends AbstractHandler implements IHandler {

	/**
	 * Key of the direction parameter.
	 */
	private static final String DIRECTION_PARAMETER = "rocks.inspectit.ui.rcp.ci.moveApplication.direction";

	/**
	 * Direction parameter value UP.
	 */
	private static final String DIRECTION_UP = "up";

	/**
	 * Direction parameter value DOWN.
	 */
	private static final String DIRECTION_DOWN = "down";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final String direction = event.getParameter(DIRECTION_PARAMETER);
		if (null == direction) {
			return null;
		}
		final StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (selection.isEmpty()) {
			return null;
		}

		Job moveApplicationJob = new Job("Move Application Job") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				List<Status> statuses = new ArrayList<>();

				Object selected = selection.getFirstElement();
				if (selected instanceof IApplicationProvider) {
					IApplicationProvider applicationProvider = (IApplicationProvider) selected;
					ApplicationDefinition application = applicationProvider.getApplication();
					CmrRepositoryDefinition repositoryDefinition = applicationProvider.getCmrRepositoryDefinition();

					try {
						if (repositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
							int oldIndex = applicationProvider.getIndexInParentList();
							int newIndex = 0;
							if (DIRECTION_UP.equals(direction)) {
								newIndex = oldIndex - 1;
							} else if (DIRECTION_DOWN.equals(direction)) {
								newIndex = oldIndex + 1;
							} else {
								throw new RuntimeException("Invalid direction parameter for application moving!");
							}

							if (newIndex < 0 || newIndex >= applicationProvider.getParentList().size() - 1) {
								throw new RuntimeException("Invalid index parameter for application moving!");
							}
							application = repositoryDefinition.getConfigurationInterfaceService().moveApplicationDefinition(application, newIndex);

							InspectIT.getDefault().getInspectITConfigurationInterfaceManager().applicationMoved(application, oldIndex, newIndex, repositoryDefinition);

						} else {
							statuses.add(new Status(IStatus.ERROR, InspectIT.ID, "Error moving application " + application.getApplicationName() + " from the CMR. Repository is offline!"));
						}
					} catch (Exception e) {
						statuses.add(new Status(IStatus.ERROR, InspectIT.ID, "Error moving application.", e));
					}
				}

				if (CollectionUtils.isNotEmpty(statuses)) {
					if (1 == statuses.size()) {
						return statuses.iterator().next();
					} else {
						return new MultiStatus(InspectIT.ID, IStatus.OK, statuses.toArray(new Status[statuses.size()]), "Moving application failed.", null);
					}
				} else {
					return Status.OK_STATUS;
				}

			}
		};
		moveApplicationJob.setUser(true);
		moveApplicationJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_APPLICATION));
		moveApplicationJob.schedule();

		return null;
	}

}
