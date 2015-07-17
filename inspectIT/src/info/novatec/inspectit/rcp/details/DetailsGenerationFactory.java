package info.novatec.inspectit.rcp.details;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.details.generator.IDetailsGenerator;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Factory for generation of the details composites. This class is initialized by Spring.
 * 
 * @author Ivan Senic
 * 
 */
public final class DetailsGenerationFactory {

	/**
	 * All the generators. Initialized by Spring.
	 */
	private List<IDetailsGenerator> generators;

	/**
	 * Creates list of detail composites displaying different types of the information for the given
	 * default data.
	 * 
	 * @param defaultData
	 *            Data to generate details for.
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition}
	 * @param parent
	 *            Parent composite
	 * @param toolkit
	 *            {@link FormToolkit}
	 * @return List of generated composites.
	 */
	public List<DetailsTable> createDetailComposites(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		// this is a work-around to include more info about the contained invocation sequence data
		DefaultData secondary = null;
		if (defaultData instanceof InvocationSequenceData) {
			InvocationSequenceData invocationSequenceData = (InvocationSequenceData) defaultData;
			if (null != invocationSequenceData.getTimerData()) {
				secondary = invocationSequenceData.getTimerData();
			}
			if (null != invocationSequenceData.getSqlStatementData()) {
				secondary = invocationSequenceData.getSqlStatementData();
			}
			if (CollectionUtils.isNotEmpty(invocationSequenceData.getExceptionSensorDataObjects())) {
				secondary = invocationSequenceData.getExceptionSensorDataObjects().get(0);
			}
		}

		List<DetailsTable> result = new ArrayList<>();
		for (IDetailsGenerator generator : generators) {
			if (generator.canGenerateFor(defaultData)) {
				result.add(generator.generate(defaultData, repositoryDefinition, parent, toolkit));
			} else if (null != secondary && generator.canGenerateFor(secondary)) {
				result.add(generator.generate(secondary, repositoryDefinition, parent, toolkit));
			}
		}
		return result;
	}

	/**
	 * Sets {@link #generators}.
	 * 
	 * @param generators
	 *            New value for {@link #generators}
	 */
	public void setGenerators(List<IDetailsGenerator> generators) {
		this.generators = generators;
	}

}
