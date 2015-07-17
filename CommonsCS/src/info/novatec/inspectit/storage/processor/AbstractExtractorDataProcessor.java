package info.novatec.inspectit.storage.processor;

import info.novatec.inspectit.communication.DefaultData;

import java.util.List;

/**
 * This type of chained processor does not pass the data to process to the chained processors, but
 * it will maybe pass other types of data to the processors.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractExtractorDataProcessor extends AbstractChainedDataProcessor {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 2436395595364720835L;

	/**
	 * @param dataProcessors
	 *            List of chained processors.
	 */
	public AbstractExtractorDataProcessor(List<AbstractDataProcessor> dataProcessors) {
		super(dataProcessors);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean shouldBePassedToChainedProcessors(DefaultData defaultData) {
		return false;
	}

}
