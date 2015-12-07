package info.novatec.inspectit.rcp.ci.handler;

import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.provider.IApplicationProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import java.util.ArrayList;
import java.util.Iterator;
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
	private static final String DIRECTION_PARAMETER = "info.novatec.inspectit.rcp.ci.moveApplication.direction";

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

		if (selection.size() != 1) {
			return null;
		}

		Job moveApplicationJob = new Job("Move Application Job") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				List<Status> statuses = new ArrayList<>();
				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
					Object selected = it.next();
					if (selected instanceof IApplicationProvider) {
						IApplicationProvider applicationProvider = (IApplicationProvider) selected;
						final ApplicationDefinition application = applicationProvider.getApplication();
						final CmrRepositoryDefinition repositoryDefinition = applicationProvider.getCmrRepositoryDefinition();

						if (null == repositoryDefinition) {
							statuses.add(new Status(IStatus.ERROR, InspectIT.ID, "Error moving application."));
						} else {

							try {
								if (repositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
									int oldIndex = applicationProvider.getIndexInParentList();
									int newIndex = 0;
									if (DIRECTION_UP.equals(direction)) {
										newIndex = oldIndex - 1;
									} else if (DIRECTION_DOWN.equals(direction)) {
										newIndex = oldIndex + 1;
									} else {
										return null;
									}

									if (newIndex < 0 || newIndex >= applicationProvider.getParentListSize() - 1) {
										return null;
									}
									repositoryDefinition.getBusinessContextMangementService().moveApplicationDefinition(application, newIndex);

									InspectIT.getDefault().getInspectITConfigurationInterfaceManager().applicationMoved(application, oldIndex, newIndex, repositoryDefinition);

								}
							} catch (Exception e) {
								statuses.add(new Status(IStatus.ERROR, InspectIT.ID, "Error moving application.", e));
							}
						}
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
		moveApplicationJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_ADDRESSBOOK));
		moveApplicationJob.schedule();

		return null;
	}

}
