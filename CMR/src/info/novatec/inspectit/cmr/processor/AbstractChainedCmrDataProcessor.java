package info.novatec.inspectit.cmr.processor;

import info.novatec.inspectit.communication.DefaultData;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.StatelessSession;

/**
 * Abstract data processor that passes data to chained processors.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractChainedCmrDataProcessor extends AbstractCmrDataProcessor {

	/**
	 * List of chained processors.
	 */
	private List<AbstractCmrDataProcessor> dataProcessors;

	/**
	 * Default constructor.
	 */
	public AbstractChainedCmrDataProcessor() {
		dataProcessors = new ArrayList<AbstractCmrDataProcessor>();
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param dataProcessors
	 *            List of chained processors.
	 */
	public AbstractChainedCmrDataProcessor(List<AbstractCmrDataProcessor> dataProcessors) {
		this.dataProcessors = dataProcessors;
	}

	/**
	 * Should the data be passed to the chained processors.
	 * 
	 * @param defaultData
	 *            {@link DefaultData}.
	 * @return True if it should be passed, false otherwise.
	 */
	protected abstract boolean shouldBePassedToChainedProcessors(DefaultData defaultData);

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, StatelessSession session) {
		if (shouldBePassedToChainedProcessors(defaultData)) {
			passToChainedProcessors(defaultData, session);
		}
	}

	/**
	 * Passed the default data to all chained processors.
	 * 
	 * @param defaultData
	 *            Data to pass.
	 * @param session
	 *            {@link StatelessSession} to save data in DB if needed.
	 */
	protected void passToChainedProcessors(DefaultData defaultData, StatelessSession session) {
		if (null != dataProcessors) {
			for (AbstractCmrDataProcessor dataProcessor : dataProcessors) {
				dataProcessor.process(defaultData, session);
			}
		}
	}

}
