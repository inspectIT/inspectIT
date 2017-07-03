package rocks.inspectit.ui.rcp.editor.banner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.externalservice.ExternalServiceStatus;
import rocks.inspectit.shared.all.externalservice.ExternalServiceType;
import rocks.inspectit.ui.rcp.editor.AbstractSubView;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;
import rocks.inspectit.ui.rcp.util.SafeExecutor;

/**
 * Banner view which will be shown if InfluxDB is enabled to notify the user that the data is not
 * available in the UI.
 *
 * @author Marius Oehler
 *
 */
public class InfluxBannerSubView extends AbstractSubView {

	/**
	 * The text of the label which is shown.
	 */
	private static final String INFO_TEXT = "InfluxDB is enabled! Data written into the InfluxDB will not be shown here!";

	/**
	 * Executor used for the service call.
	 */
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	/**
	 * The {@link Composite}.
	 */
	private Composite composite;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(1, false));

		Composite innerComposite = toolkit.createComposite(composite);
		innerComposite.setLayout(new GridLayout(2, false));
		innerComposite.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true));

		Label labelIcon = toolkit.createLabel(innerComposite, "");
		labelIcon.setImage(Display.getCurrent().getSystemImage(SWT.ICON_WARNING));
		labelIcon.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

		Label labelInfoText = toolkit.createLabel(innerComposite, INFO_TEXT);
		FontData fontData = labelInfoText.getFont().getFontData()[0];
		Font font = new Font(Display.getCurrent(), new FontData(fontData.getName(), (int) (fontData.getHeight() * 1.5D), SWT.BOLD));
		labelInfoText.setFont(font);
		labelInfoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		return Collections.emptySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRefresh() {
		RepositoryDefinition repositoryDefinition = getRootEditor().getInputDefinition().getRepositoryDefinition();

		if (repositoryDefinition instanceof CmrRepositoryDefinition) {
			final CmrRepositoryDefinition cmrRepositoryDefinition = (CmrRepositoryDefinition) repositoryDefinition;

			executorService.execute(new Runnable() {
				@Override
				public void run() {
					Map<ExternalServiceType, ExternalServiceStatus> statusMap = cmrRepositoryDefinition.getCmrManagementService().getCmrStatusData().getExternalServiceStatusMap();
					final ExternalServiceStatus serviceStatus = statusMap.get(ExternalServiceType.INFLUXDB);

					SafeExecutor.syncExec(new Runnable() {
						@Override
						public void run() {
							if (serviceStatus == ExternalServiceStatus.DISABLED) {
								// hide

								composite.setLayoutData(new GridData(0, 0));
								composite.getParent().layout();
							} else {
								// show

								composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
								composite.getParent().layout();
							}
						}
					});
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataInput(List<? extends Object> data) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control getControl() {
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISelectionProvider getSelectionProvider() {
		return null;
	}
}
