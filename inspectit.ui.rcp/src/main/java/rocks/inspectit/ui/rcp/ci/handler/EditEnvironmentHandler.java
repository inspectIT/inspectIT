package rocks.inspectit.ui.rcp.ci.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.dialog.EditNameDescriptionDialog;
import rocks.inspectit.ui.rcp.job.BlockingJob;
import rocks.inspectit.ui.rcp.provider.IEnvironmentProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Handler for the environment edit.
 *
 * @author Ivan Senic
 *
 */
public class EditEnvironmentHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}

		Object selected = selection.getFirstElement();
		if (selected instanceof IEnvironmentProvider) {
			IEnvironmentProvider environmentProvider = (IEnvironmentProvider) selected;
			Environment environment = environmentProvider.getEnvironment();
			final CmrRepositoryDefinition repositoryDefinition = environmentProvider.getCmrRepositoryDefinition();

			BlockingJob<Collection<Environment>> job = new BlockingJob<>("Fetching environments..", new Callable<Collection<Environment>>() {
				@Override
				public Collection<Environment> call() throws Exception {
					return repositoryDefinition.getConfigurationInterfaceService().getAllEnvironments();
				}
			});

			ArrayList<Environment> environments = (ArrayList<Environment>) job.scheduleAndJoin();
			String[] environmentNames = new String[environments.size()];
			for (int i = 0; i < environments.size(); i++) {
				environmentNames[i] = environments.get(i).getName();
			}
			EditNameDescriptionDialog dialog = new EditNameDescriptionDialog(HandlerUtil.getActiveShell(event), environment.getName(), environment.getDescription(), "Edit Environment",
					"Enter new environment name and/or description", environmentNames);
			if (Window.OK == dialog.open()) {
				environment.setName(dialog.getName());
				if (StringUtils.isNotBlank(dialog.getDescription())) {
					environment.setDescription(dialog.getDescription());
				}

				try {
					Environment updated = repositoryDefinition.getConfigurationInterfaceService().updateEnvironment(environment);

					InspectIT.getDefault().getInspectITConfigurationInterfaceManager().environmentUpdated(updated, repositoryDefinition);
				} catch (BusinessException e) {
					throw new ExecutionException("Update of the environment state failed.", e);
				}
			}
		}

		return null;
	}

}
