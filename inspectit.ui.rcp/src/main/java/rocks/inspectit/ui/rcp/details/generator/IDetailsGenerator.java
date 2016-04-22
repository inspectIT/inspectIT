package rocks.inspectit.ui.rcp.details.generator;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.ui.rcp.details.DetailsTable;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * Interface for details generator.
 * <p>
 * Each generator generates one composite.
 *
 * @author Ivan Senic
 *
 */
public interface IDetailsGenerator {

	/**
	 * Specifies if generator can generate composite for given {@link DefaultData} object.
	 *
	 * @param defaultData
	 *            {@link DefaultData}.
	 * @return Return <code>true</code> if generator can generate the details for the
	 *         {@link DefaultData} object.
	 */
	boolean canGenerateFor(DefaultData defaultData);

	/**
	 * Creates details composite on the given parent composite for the {@link DefaultData} object.
	 *
	 * @param defaultData
	 *            {@link DefaultData}
	 * @param repositoryDefinition
	 *            Repository definition data belongs to.
	 * @param parent
	 *            Parent {@link Composite}
	 * @param toolkit
	 *            {@link FormToolkit}
	 * @return Returns the created composite.
	 */
	DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit);
}
