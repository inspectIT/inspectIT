package info.novatec.inspectit.rcp.details.generator.impl;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.details.DetailsCellContent;
import info.novatec.inspectit.rcp.details.DetailsTable;
import info.novatec.inspectit.rcp.details.generator.IDetailsGenerator;
import info.novatec.inspectit.rcp.model.ModifiersImageFactory;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

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
