package info.novatec.inspectit.rcp.editor.root;

/**
 * Interface for defining the classification of sub views, so that different actions can be
 * performed with differently classified views.
 * 
 * @author Ivan Senic
 * 
 */
public interface SubViewClassificationController {

	/**
	 * Defines different classification options for the view.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum SubViewClassification {

		/**
		 * In master view input is controlled by its input controller.
		 */
		MASTER,

		/**
		 * In slave view input is controlled by some other view/input controller.
		 */
		SLAVE;
	}

	/**
	 * 
	 * @return Returns the sub view classification.
	 * @see SubViewClassification
	 */
	SubViewClassification getSubViewClassification();

}
