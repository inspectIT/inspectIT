package rocks.inspectit.ui.rcp.details.generator.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.MethodSensorData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.details.DetailsCellContent;
import rocks.inspectit.ui.rcp.details.DetailsTable;
import rocks.inspectit.ui.rcp.details.generator.IDetailsGenerator;
import rocks.inspectit.ui.rcp.model.ModifiersImageFactory;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * Generates information about the method from the {@link MethodSensorData}.
 * 
 * @author Ivan Senic
 * 
 */
public class MethodInfoDetailsGenerator implements IDetailsGenerator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return defaultData instanceof MethodSensorData && ((MethodSensorData) defaultData).getMethodIdent() != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		MethodSensorData methodSensorData = (MethodSensorData) defaultData;
		MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(methodSensorData.getMethodIdent());

		DetailsTable table = new DetailsTable(parent, toolkit, "Method Info", 1);

		// package & class
		table.addContentRow("Package:", InspectIT.getDefault().getImage(InspectITImages.IMG_PACKAGE), new DetailsCellContent[] { new DetailsCellContent(methodIdent.getPackageName()) });
		table.addContentRow("Class:", InspectIT.getDefault().getImage(InspectITImages.IMG_CLASS), new DetailsCellContent[] { new DetailsCellContent(methodIdent.getClassName()) });

		// method
		String params = "";
		if (CollectionUtils.isNotEmpty(methodIdent.getParameters())) {
			params = methodIdent.getParameters().toString();
			params = params.substring(1, params.length() - 1);
		}
		String method = methodIdent.getMethodName() + "(" + params + ")";
		table.addContentRow("Method:", ModifiersImageFactory.getImage(methodIdent.getModifiers()), new DetailsCellContent[] { new DetailsCellContent(method) });

		// return type
		String returnType = methodIdent.getReturnType();
		if (StringUtils.isBlank(returnType)) {
			returnType = "void";
		}
		table.addContentRow("Return Type:", null, new DetailsCellContent[] { new DetailsCellContent(returnType) });

		// instrumentation
		DetailsCellContent instrumentationContent = new DetailsCellContent();
		instrumentationContent.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		if (methodIdent.hasActiveSensorTypes()) {
			instrumentationContent.setText("Active ");
			instrumentationContent.setImageToolTip("Method is currently instrumented and captures data.");
		} else {
			instrumentationContent.setText("Not-active ");
			instrumentationContent.setImageToolTip("Method is currently not instrumented and  doesn't capture data.");
		}
		table.addContentRow("Instrumentation:", null, new DetailsCellContent[] { instrumentationContent });

		return table;
	}

}
